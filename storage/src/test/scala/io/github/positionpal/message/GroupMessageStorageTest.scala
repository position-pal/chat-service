package io.github.positionpal.message

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import io.github.positionpal.client.ClientID
import io.github.positionpal.message.PipeUtils.TestProtocol.{Start, TestComplete}
import io.github.positionpal.service.GroupService
import io.github.positionpal.services.GroupHandlerService
import io.github.positionpal.storage.MessageStorage
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

object PipeUtils:

  enum TestProtocol:
    case Start
    case Joined
    case Connected
    case MessagesSent
    case TestComplete(result: Seq[String])

  def sendMessages(
      remaining: Seq[String],
      userID: ClientID,
      groupID: String,
  )(using groupService: GroupHandlerService, executionContext: ExecutionContext): Future[Unit] =
    remaining match
      case Nil =>
        Future.successful(())
      case msg :: tail =>
        val messageNow = ChatMessageADT.now(msg, userID, groupID)
        groupService.message(groupID)(messageNow).flatMap(_ => sendMessages(tail, userID, groupID))

  def testPipe(userID: ClientID, groupID: String, testName: String)(
      testingBehaviour: (MessageStorage, String) => Unit,
  )(using groupService: GroupHandlerService, system: ActorSystem[?]): Behavior[TestProtocol] =
    import TestProtocol.*

    given executionContext: ExecutionContext = system.executionContext
    val storage = GroupMessageStorage()

    Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case Start =>
          context.pipeToSelf(groupService.join(groupID)(userID)): _ =>
            Joined
          context.log.info("[ACTOR]: Start")
          Behaviors.same

        case Joined =>
          context.pipeToSelf(
            groupService.connect(groupID)(userID, system.systemActorOf[String](Behaviors.empty, s"test-$testName")),
          ): _ =>
            Connected

          context.log.info("[ACTOR]: joining")
          Behaviors.same

        case Connected =>
          context.log.info("[ACTOR]: connecting")
          val messages = Seq("message1", "message2", "message3", "message4", "message5")
          context.pipeToSelf(sendMessages(messages, userID, groupID)): _ =>
            MessagesSent
          Behaviors.same

        case MessagesSent =>
          context.log.info("[ACTOR]: sent all messages")
          testingBehaviour(storage, groupID)
          Behaviors.stopped

        case TestComplete(msgs) =>
          context.log.info(msgs.toString())
          Behaviors.stopped

class GroupMessageStorageTest
    extends ScalaTestWithActorTestKit(
      ConfigFactory.parseString("""
     akka.actor {
        allow-java-serialization = on
        provider = "cluster"
        serializers {
          jackson-cbor = "akka.serialization.jackson.JacksonCborSerializer"
          borer-cbor = "io.github.positionpal.serializer.AkkaSerializer"
        }
        serialization-bindings {
          "io.github.positionpal.borer.BorerSerialization" = borer-cbor
        }
     }

     akka.remote.artery.canonical {
        hostname = "127.0.0.1"
        port = 0
     }

     akka.cluster {
        seed-nodes = ["akka://GroupMessageStorageTest@127.0.0.1:0"]
        downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"

        jmx.enabled = off
        shutdown-after-unsuccessful-join-seed-nodes = 20s
     }

     akka.persistence{
        journal.plugin = "akka.persistence.cassandra.journal"
        journal.auto-start-journals = ["akka.persistence.cassandra.journal"]

        cassandra {
          journal.keyspace = "chatservice"
          journal.keyspace-autocreate = true
          journal.tables-autocreate = true

          connection-timeout = 30s
          init-retry-interval = 2s
          init-retry-max = 15
        }
     }
    """).withFallback(ConfigFactory.defaultApplication()),
    )
    with AnyWordSpecLike
    with Matchers:

  private val cluster = Cluster(system)

  override def beforeAll(): Unit =
    super.beforeAll()
    cluster.join(cluster.selfMember.address)

  import PipeUtils.*
  import scala.util.Success

  given service: GroupHandlerService = GroupService(system)
  given ExecutionContext = system.executionContext

  "GroupMessageStorage" should:
    "retrieve the last n messages for a group" in:

      val probe = createTestProbe[TestProtocol]()
      val pipe = spawn:
        testPipe(ClientID("123"), "testingPipe", "nMessages"): (storage, groupID) =>
          val result = storage.getLastMessages(groupID)(3)
          result.onComplete:
            case Success(result) => probe.ref ! TestComplete(result)
            case _ => fail("The operation completed with a failure")

      pipe ! Start

      probe.expectMessageType[TestComplete](10.seconds) match
        case TestComplete(messages) =>
          messages should have size 3
          messages should contain allOf ("message5", "message4", "message3")
