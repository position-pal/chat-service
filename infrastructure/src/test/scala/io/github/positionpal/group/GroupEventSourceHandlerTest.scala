package io.github.positionpal.group

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.pattern.StatusReply.{Error, Success}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import io.github.positionpal.client.ClientADT.{ClientStatus, OutputReference}
import io.github.positionpal.client.ClientID
import io.github.positionpal.group.GroupEvent as Event
import io.github.positionpal.group.GroupEventSourceHandler.{Command, State}
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

    "allow a client to connect to the group after joining" in:
      val clientID = ClientID(value = "1a2b4")
      val communicationChannel = testKit.createTestProbe[String]().ref

      eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))

      val stateAfterJoin = eventSourcedBehaviorTestKit.getState()
      stateAfterJoin.getClient(clientID) match
        case Right(client) =>
          client.outputRef should ===(OutputReference.EMPTY)
          client.status should ===(ClientStatus.OFFLINE)
        case Left(error) => fail(s"Expected client to exist, but got error: $error")

      val result = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID, communicationChannel, replyTo))

      result.reply.isSuccess should ===(true)
      result.reply should ===(Success(ClientSuccessfullyConnected(clientID)))

      val stateAfterConnect = eventSourcedBehaviorTestKit.getState()
      stateAfterConnect.getClient(clientID) match
        case Right(client) =>
          client.outputRef should ===(OutputReference.OUT(communicationChannel))
          client.status should ===(ClientStatus.OFFLINE)
        case Left(error) => fail(s"Expected client to exist, but got error: $error")

    "return an error when a client tries to connect without joining first" in:
      val clientID = ClientID(value = "1a2b4")
      val communicationChannel = testKit.createTestProbe[String]().ref

      val result = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID, communicationChannel, replyTo))

      result.reply.isError should ===(true)
      result.reply should ===(Error(s"client $clientID doesn't belongs to the group"))

      val state = eventSourcedBehaviorTestKit.getState()
      state.isPresent(clientID) should ===(false)

    "allow a client to reconnect with a different communication channel" in:
      val clientID = ClientID(value = "1a2b4")
      val firstChannel = testKit.createTestProbe[String]().ref
      val secondChannel = testKit.createTestProbe[String]().ref

      eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))
      eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID, firstChannel, replyTo))

      val stateAfterFirstConnect = eventSourcedBehaviorTestKit.getState()
      stateAfterFirstConnect.getClient(clientID) match {
        case Right(client) =>
          client.outputRef should ===(OutputReference.OUT(firstChannel))
          client.status should ===(ClientStatus.OFFLINE)
        case Left(error) => fail(s"Expected client to exist, but got error: $error")
      }

      val result = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID, secondChannel, replyTo))

      result.reply.isSuccess should ===(true)
      result.reply should ===(Success(ClientSuccessfullyConnected(clientID)))

      val stateAfterReconnect = eventSourcedBehaviorTestKit.getState()
      stateAfterReconnect.getClient(clientID) match
        case Right(client) =>
          client.outputRef should ===(OutputReference.OUT(secondChannel))
          client.status should ===(ClientStatus.OFFLINE)
        case Left(error) => fail(s"Expected client to exist, but got error: $error")
