package utils

import scala.concurrent.Future
import scala.concurrent.duration.*

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, PostStop}
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.typesafe.config.ConfigFactory
import io.github.positionpal.utils.AkkaUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

enum TestProtocol:
  case Message(content: String)
  case Reply(content: String)
  case State(content: String)
  case GetState
  case Stop

import TestProtocol.*

object TestingBehaviors:
  def echoActor: Behavior[TestProtocol] =
    Behaviors.receive: (context, message) =>
      message match
        case Message(content) =>
          context.log.info(s"Received: $content")
          Behaviors.same
        case _ => Behaviors.same

  def supervisedActor(probe: TestProbe[String]): Behavior[TestProtocol] =
    Behaviors.setup: _ =>
      Behaviors.receive[TestProtocol]: (_, message) =>
        message match
          case Message(content) if content == "fail" =>
            throw new RuntimeException("Simulated failure")
          case Message(content) =>
            probe.ref ! content
            Behaviors.same
          case _ => Behaviors.same
      .receiveSignal:
        case (_, PostStop) =>
          probe.ref ! "stopped"
          Behaviors.same

class AkkaUtilsTest extends AsyncWordSpecLike with AsyncIOSpec with Matchers with BeforeAndAfterAll:

  val testKit = ActorTestKit(
    "TestSystem",
    ConfigFactory.parseString(
      """
        akka {
          actor {
            provider = local
          }
          coordinated-shutdown.phases {
            before-service-unbind.timeout = 1s
            actor-system-terminate.timeout = 1s
          }
          loglevel = "WARNING"
        }
      """,
    ),
  )

  import TestingBehaviors.*

  "ActorSystemSetup with TestKit" should:
    "create a working actor system" in:
      val test =
        for system <- AkkaUtils.startTypedSystem(
            systemName = "test-system",
            behavior = echoActor,
            config = testKit.config,
            useIOExecutionContext = false,
            timeoutAwaitCatsEffect = 5.seconds,
            timeoutAwaitAkkaTermination = 5.seconds,
          )
        yield system
      test.use: system =>
        IO:
          system.name should startWith("test-system")
          system.whenTerminated.isCompleted shouldBe false

    "handle actor messages correctly" in:
      val probe = testKit.createTestProbe[TestProtocol]()
      val behavior = Behaviors.receive[TestProtocol]: (_, message) =>
        probe.ref ! message
        Behaviors.same

      val test = AkkaUtils.startTypedSystem(
        systemName = "message-test",
        behavior = behavior,
        config = testKit.config,
        useIOExecutionContext = false,
        timeoutAwaitCatsEffect = 5.seconds,
        timeoutAwaitAkkaTermination = 5.seconds,
      )

      test.use: system =>
        IO:
          system.tell(Message("test"))
          probe.expectMessage(Message("test"))
          succeed

    "handle supervision and recovery" in:
      val probe = testKit.createTestProbe[String]()
      val test = AkkaUtils.startTypedSystem(
        systemName = "supervision-test",
        behavior = supervisedActor(probe),
        config = testKit.config,
        useIOExecutionContext = false,
        timeoutAwaitCatsEffect = 5.seconds,
        timeoutAwaitAkkaTermination = 5.seconds,
      )

      test.use: system =>
        IO:
          system.tell(Message("normal"))
          probe.expectMessage("normal")

          system.tell(Message("fail"))
          probe.expectMessage("stopped")
          succeed

    "handle coordinated shutdown" in:
      var shutdownCalled = false
      val test = AkkaUtils.startTypedSystem(
        systemName = "shutdown-test",
        behavior = Behaviors.setup[TestProtocol]: context =>
          val shutdown = CoordinatedShutdown(context.system)
          shutdown.addTask(
            CoordinatedShutdown.PhaseBeforeServiceUnbind,
            "test-task",
          ): () =>
            shutdownCalled = true
            Future.successful(Done)
          Behaviors.empty
        ,
        config = testKit.config,
        useIOExecutionContext = false,
        timeoutAwaitCatsEffect = 5.seconds,
        timeoutAwaitAkkaTermination = 5.seconds,
      )

      test.use: _ =>
        IO.sleep(100.millis) *> IO:
          shutdownCalled shouldBe false
          succeed
      *> IO.sleep(200.millis) *> IO:
        shutdownCalled shouldBe true
        succeed

    "handle system termination gracefully" in:
      val probe = testKit.createTestProbe[String]()

      val test = AkkaUtils.startTypedSystem(
        systemName = "termination-test",
        behavior = Behaviors.setup[TestProtocol] { context =>
          context.spawn(
            Behaviors.receiveSignal[TestProtocol]:
              case (_, PostStop) =>
                probe.ref ! "child-stopped"
                Behaviors.same
            ,
            "child",
          )
          Behaviors.empty
        },
        config = testKit.config,
        useIOExecutionContext = false,
        timeoutAwaitCatsEffect = 5.seconds,
        timeoutAwaitAkkaTermination = 5.seconds,
      )

      test.use: _ =>
        IO.sleep(100.millis)
      *> IO.sleep(200.millis) *> IO:
        probe.expectMessage("child-stopped")
        succeed
