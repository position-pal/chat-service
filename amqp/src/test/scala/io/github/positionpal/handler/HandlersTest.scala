package io.github.positionpal.handler

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.util.ByteString
import io.github.positionpal.AvroSerializer
import io.github.positionpal.client.ClientID
import io.github.positionpal.consumer.QueueConsumer
import io.github.positionpal.events.EventType
import io.github.positionpal.message.ChatMessageADT
import io.github.positionpal.utils.AmqpWriter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

class HandlersTest
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with BeforeAndAfterEach:

  private val serializer = AvroSerializer()
  private val processedMessages = TrieMap.empty[EventType, Any]

  import io.github.positionpal.services.GroupHandlerService
  import io.github.positionpal.client.ClientCommunications.CommunicationProtocol

  private case class TestingGroupService() extends GroupHandlerService[Future, CommunicationProtocol]:
    override def delete(groupID: String): Future[Unit] =
      processedMessages.put(EventType.GROUP_DELETED, groupID)
      Future.unit

    override def join(groupID: String)(clientID: ClientID): Future[List[ClientID]] =
      processedMessages.put(EventType.GROUP_CREATED, (groupID, clientID))
      Future.successful(List(clientID))

    override def leave(groupID: String)(clientID: ClientID): Future[ClientID] =
      processedMessages.put(EventType.MEMBER_REMOVED, (groupID, clientID))
      Future.successful(clientID)

    override def connect(
        groupID: String,
    )(clientID: ClientID, channel: ActorRef[CommunicationProtocol]): Future[ClientID] = ???
    override def disconnect(groupID: String)(clientID: ClientID): Future[ClientID] = ???
    override def message(groupID: String)(message: ChatMessageADT.Message[ClientID, String]): Future[Unit] = ???

  import io.github.positionpal.connection.AmqpConfiguration
  import io.github.positionpal.connection.Connection.toProvider
  import io.github.positionpal.consumer.QueueConsumer.Queue
  import io.github.positionpal.consumer.QueueConsumer.Exchange.GROUP_UPDATE

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
        Queue(name = "testing_queue", exchanges = List(GROUP_UPDATE)),
      )
      val handler = Handlers.basic(TestingGroupService())
      QueueConsumer.create(provider, queues, handler).run()

  override def beforeEach(): Unit =
    super.beforeEach()
    processedMessages.clear()

  import io.github.positionpal.events.{GroupCreated, GroupDeleted, AddedMemberToGroup, RemovedMemberToGroup}
  import io.github.positionpal.entities.User
  import io.github.positionpal.entities.{GroupId, UserId}

  "MessageHandler" should:
    "Handle correctly a GROUP_CREATE message" in:

      val messageType = EventType.GROUP_CREATED
      val groupFounder = User.create(
        UserId.create("123"),
        "tizio",
        "caio",
        "tizio.caio@email.it",
      )
      val groupId = GroupId.create("group-123")
      val event = GroupCreated.create(groupId, groupFounder)
      val message = ByteString(serializer.serializeGroupCreated(event))
      whenReady(AmqpWriter.send(message, messageType, GROUP_UPDATE, "testing_queue")):
        _.confirmed shouldBe true

      eventually:
        processedMessages.contains(messageType) shouldBe true
        processedMessages.get(messageType) match
          case Some((gid: String, clientID: ClientID)) =>
            gid shouldBe groupId.value()
            clientID shouldBe ClientID("123")

          case _ => fail("Event didn't happen")

    "Handle correctly a GROUP_DELETE message" in:

      val messageType = EventType.GROUP_DELETED
      val groupId = GroupId.create("group-123-ex")
      val event = GroupDeleted.create(groupId)
      val message = ByteString(serializer.serializeGroupDeleted(event))

      whenReady(AmqpWriter.send(message, messageType, GROUP_UPDATE, "testing_queue")):
        _.confirmed shouldBe true

      eventually:
        processedMessages.contains(messageType) shouldBe true
        processedMessages.get(messageType) match
          case Some(gid: String) => gid shouldBe groupId.value()
          case _ => fail("Event didn't happen")

    "Handle correctly a MEMBER_ADDED message" in:

      val messageType = EventType.MEMBER_ADDED
      val groupId = GroupId.create("group-123")
      val memberAdded = User.create(
        UserId.create("666"),
        "ugo",
        "sonato",
        "ugo.sonato@email.it",
      )
      val event = AddedMemberToGroup.create(groupId, memberAdded)
      val message = ByteString(serializer.serializeAddedMemberToGroup(event))
      whenReady(AmqpWriter.send(message, messageType, GROUP_UPDATE, "testing_queue")):
        _.confirmed shouldBe true

      eventually:
        processedMessages.contains(EventType.GROUP_CREATED) shouldBe true
        processedMessages.get(EventType.GROUP_CREATED) match
          case Some((gid: String, clientID: ClientID)) =>
            gid shouldBe groupId.value()
            clientID shouldBe ClientID("666")

          case _ => fail("Event didn't happen")

    "Handle correctly a MEMBER_REMOVED message" in:

      val messageType = EventType.MEMBER_REMOVED
      val groupId = GroupId.create("group-123")
      val memberRemoved = User.create(
        UserId.create("666"),
        "ugo",
        "sonato",
        "ugo.sonato@email.it",
      )
      val event = RemovedMemberToGroup.create(
        groupId,
        memberRemoved,
      )

      val message = ByteString(serializer.serializeRemovedMemberToGroup(event))
      whenReady(AmqpWriter.send(message, messageType, GROUP_UPDATE, "testing_queue")):
        _.confirmed shouldBe true

      eventually:
        processedMessages.contains(EventType.MEMBER_REMOVED) shouldBe true
        processedMessages.get(EventType.MEMBER_REMOVED) match
          case Some((gid: String, clientID: ClientID)) =>
            gid shouldBe groupId.value()
            clientID shouldBe ClientID("666")
          case _ => fail("Event didn't happen")
