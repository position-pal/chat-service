import scala.concurrent.duration.DurationInt

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import io.github.positionpal.grpc.ServiceHandler
import io.github.positionpal.proto.RetrieveLastMessagesRequest
import io.github.positionpal.proto.StatusCode.OK
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

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
      val request = RetrieveLastMessagesRequest("a123", "123", "4")
      val reply = service.retrieveLastMessages(request)

      reply.futureValue.code should be(OK)
      reply.futureValue.messages.map(_.content.strip()) should be(
        Seq("Ok then, let's do this!", "It's getting late, I think", "See you then", "bye!"),
      )

    "handle request for non-existent group" in:
      val request = RetrieveLastMessagesRequest("non-existen", "111", "2")
      val reply = service.retrieveLastMessages(request)

      reply.futureValue.code should be(OK)
      reply.futureValue.messages.length should be(0)
