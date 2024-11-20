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

  val conf = ConfigFactory.load("local-config.conf")
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
