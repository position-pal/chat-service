package io.github.positionpal.group

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.pattern.StatusReply.{Error, Success}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import io.github.positionpal.client.ClientID
import io.github.positionpal.command.{ClientJoinsGroup, ClientLeavesGroup, Command}
import io.github.positionpal.event.GroupEvent as Event
import io.github.positionpal.reply.{ClientSuccessfullyJoined, ClientSuccessfullyLeaved, Reply}
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

  opaque type State = GroupADT.Group[ClientID, String]
  private val eventSourcedBehaviorTestKit = EventSourcedBehaviorTestKit[Command, Event, State](
    system,
    GroupEventSourceHandler("test"),
  )

  override protected def beforeEach(): Unit =
    super.beforeEach()
    eventSourcedBehaviorTestKit.clear()

  "Group Event Source Handler" should:

    "allow a new user to join the group" in:
      val clientID = ClientID(value = "1a2b4")
      val result1 = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))
      result1.reply.isSuccess should ===(true)
      result1.reply should ===(Success(ClientSuccessfullyJoined(List(clientID))))

    "return an error when a client tries to join again in the group" in:

      val clientID = ClientID(value = "1a2b4")

      eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))
      val invalidRequest = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))

      invalidRequest.reply.isError should ===(true)
      invalidRequest.reply should ===(Error(s"client $clientID already joined"))

    "allow a user to leave from the group" in:

      val clientID = ClientID(value = "1a2b4")
      eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))
      val result = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientLeavesGroup(clientID, replyTo))

      result.reply.isSuccess should ===(true)
      result.reply should ===(Success(ClientSuccessfullyLeaved(clientID)))

  "return an error when receive a leave request from a client that doesn't belongs to the group" in:
    val clientID = ClientID(value = "1a2b4")

    val result = eventSourcedBehaviorTestKit
      .runCommand[StatusReply[Reply]](replyTo => ClientLeavesGroup(clientID, replyTo))
    result.reply.isError should ===(true)
    result.reply should ===(Error(s"client $clientID doesn't belongs to the group"))
