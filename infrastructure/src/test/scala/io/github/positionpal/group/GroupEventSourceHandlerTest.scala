package io.github.positionpal.group

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.pattern.StatusReply.Success
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import io.github.positionpal.client.ClientADT.ClientID
import io.github.positionpal.group.GroupEventSourceHandler.{
  ClientJoinsGroup,
  Command,
  Event,
  Reply,
  UserSuccessfullyJoined,
}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupEventSourceHandlerTest
    extends ScalaTestWithActorTestKit(
      ConfigFactory.parseString("akka.actor.allow-java-serialization = on")
        .withFallback(EventSourcedBehaviorTestKit.config),
    )
    with AnyWordSpecLike
    with BeforeAndAfterEach
    with Matchers:

  opaque type State = GroupADT.Group[String]
  private val eventSourcedBehaviorTestKit = EventSourcedBehaviorTestKit[Command, Event, State](
    system,
    GroupEventSourceHandler("test"),
  )

  override protected def beforeEach(): Unit =
    super.beforeEach()
    eventSourcedBehaviorTestKit.clear()

  "Group Event Source Handler" should:

    "allow a new user to join to a group" in:
      val clientID = ClientID(
        id = "1a2b4",
        email = "1a2b4@email.it",
      )

      val result1 = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))
      result1.reply.isSuccess should ===(true)
      result1.reply should ===(Success(UserSuccessfullyJoined(List(clientID))))
