package io.github.positionpal.group

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import io.github.positionpal.client.ClientADT.OutputReference
import io.github.positionpal.client.ClientEventSourceHandler
import io.github.positionpal.client.ClientEventSourceHandler.{Command, Event, State}
import io.github.positionpal.command.ReceiveReference
import io.github.positionpal.event.NewReference
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ClientEventSourceHandlerTest
    extends ScalaTestWithActorTestKit(
      ConfigFactory.parseString("akka.actor.allow-java-serialization = on")
        .withFallback(EventSourcedBehaviorTestKit.config),
    )
    with AnyWordSpecLike
    with BeforeAndAfterEach
    with Matchers:

  private val eventSourcedBehaviorTestKit = EventSourcedBehaviorTestKit[Command, Event, State](
    system,
    ClientEventSourceHandler("test-client"),
  )

  override protected def beforeEach(): Unit =
    super.beforeEach()
    eventSourcedBehaviorTestKit.clear()

  "Client Event Source Handler" should:

    "be created with an empty status" in:
      val state = eventSourcedBehaviorTestKit.getState()
      state.id should be("test-client")
      state.outputRef should ===(OutputReference.EMPTY)

    "add a new output reference" in:
      val testRef = testKit.createTestProbe[Any]().ref
      val command = ReceiveReference(testRef)

      val result = eventSourcedBehaviorTestKit.runCommand(command)

      result.event should ===(NewReference(testRef))
      result.state.outputRef should ===(OutputReference.OUT(testRef))

    "handle multiple reference updates" in:
      val firstRef = testKit.createTestProbe[Any]().ref
      val secondRef = testKit.createTestProbe[Any]().ref

      eventSourcedBehaviorTestKit.runCommand(ReceiveReference(firstRef))
      val result = eventSourcedBehaviorTestKit.runCommand(ReceiveReference(secondRef))

      result.event should ===(NewReference(secondRef))
      result.state.outputRef should ===(OutputReference.OUT(secondRef))
