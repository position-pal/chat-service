import scala.concurrent.duration.DurationInt

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import io.github.positionpal.grpc.ServiceHandler
import io.github.positionpal.proto.{Message, MessageResponse, RetrieveLastMessagesRequest}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalapb.UnknownFieldSet

class ServiceHandlerTest extends AnyWordSpec with BeforeAndAfterAll with Matchers with ScalaFutures:

  val conf = ConfigFactory.parseString("""
     akka.http.server.enable-http2 = on
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

          connection-timeout = 30s
          init-retry-interval = 2s
          init-retry-max = 15
        }
     }
    """).withFallback(ConfigFactory.defaultApplication())
  val testKit: ActorTestKit = ActorTestKit(conf)

  override def afterAll(): Unit =
    super.afterAll()
    testKit.shutdownTestKit()

  given patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))
  given serverSystem: ActorSystem[?] = testKit.system

  val service = new ServiceHandler()

  "ServiceHandler" should:
    "handle requests for message history" in:
      val request = RetrieveLastMessagesRequest("123", "444", "4")
      val reply = service.retrieveLastMessages(request)

      reply.futureValue should ===(
        MessageResponse(
          Vector(
            Message("aaa\n", UnknownFieldSet(Map())),
            Message("ddd\n", UnknownFieldSet(Map())),
            Message("cxcmkxmvxv\n", UnknownFieldSet(Map())),
          ),
          UnknownFieldSet(Map()),
        ),
      )
