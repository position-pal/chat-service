package io.github.positionpal.consumer

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

import akka.Done
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.util.ByteString
import io.github.positionpal.AvroSerializer
import io.github.positionpal.connection.AmqpConfiguration
import io.github.positionpal.connection.Connection.toProvider
import io.github.positionpal.consumer.QueueConsumer.*
import io.github.positionpal.consumer.QueueConsumer.Exchange.GROUP_UPDATE
import io.github.positionpal.entities.{GroupId, User, UserId}
import io.github.positionpal.events.{AddedMemberToGroup, EventType, RemovedMemberToGroup}
import io.github.positionpal.handler.MessageHandler
import io.github.positionpal.utils.AmqpWriter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

class QueueConsumerTest
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with BeforeAndAfterEach:

  private val serializer = AvroSerializer()
  private val processedMessages = TrieMap.empty[EventType, ByteString]

  given ExecutionContext = system.executionContext

  private class TestingHandler extends MessageHandler[Future]:
    override def handle(messageType: EventType, message: ByteString): Future[Any] =
      processedMessages.put(messageType, message)
      Future.successful(Done)

  override def beforeEach(): Unit =
    super.beforeEach()
    processedMessages.clear()

  override def beforeAll(): Unit =
    super.beforeAll()
    val configuration = AmqpConfiguration.of(
      host = "localhost",
      port = 5672,
      virtualHost = "/",
      username = "guest",
      password = "admin",
    )

    configuration.toProvider.foreach: provider =>
      val queues = List(
        Queue(name = "test1", exchanges = List(GROUP_UPDATE)),
        Queue(name = "test2", exchanges = List(GROUP_UPDATE)),
      )
      val handler = new TestingHandler
      QueueConsumer.create(provider, queues, handler).run()

  "QueueConsumer" should:
    "Accept message with correct header" in:

      val messageType = EventType.MEMBER_ADDED

      val user = User.create(UserId.create("uid-test"), "name-test", "surname-test", "email-test")
      val event = AddedMemberToGroup.create(GroupId.create("123"), user)

      val message = ByteString(serializer.serializeAddedMemberToGroup(event))
      whenReady(AmqpWriter.send(message, messageType, GROUP_UPDATE, "test1")):
        _.confirmed shouldBe true

      eventually:
        processedMessages.contains(EventType.MEMBER_ADDED) shouldBe true
        processedMessages(EventType.MEMBER_ADDED) should not be empty

    "Multiple messages in different queues" in:

      val messageType1 = EventType.MEMBER_ADDED
      val messageType2 = EventType.MEMBER_REMOVED

      val user1 = User.create(UserId.create("uid-test-1"), "name-test-1", "surname-test-1", "email-test-1")
      val user2 = User.create(UserId.create("uid-test-2"), "name-test-2", "surname-test-2", "email-test-2")

      val event1 = AddedMemberToGroup.create(GroupId.create("123"), user1)
      val event2 = RemovedMemberToGroup.create(GroupId.create("456"), user2)

      val sendResult1 = AmqpWriter.send(
        ByteString(serializer.serializeAddedMemberToGroup(event1)),
        messageType1,
        GROUP_UPDATE,
        "test1",
      )

      val sendResult2 = AmqpWriter.send(
        ByteString(serializer.serializeRemovedMemberToGroup(event2)),
        messageType2,
        GROUP_UPDATE,
        "test2",
      )

      whenReady(Future.sequence(Seq(sendResult1, sendResult2))): results =>
        results.foreach(_.confirmed shouldBe true)

      eventually:
        processedMessages.size shouldBe 2
        processedMessages.contains(EventType.MEMBER_ADDED) shouldBe true
        processedMessages.contains(EventType.MEMBER_REMOVED) shouldBe true
