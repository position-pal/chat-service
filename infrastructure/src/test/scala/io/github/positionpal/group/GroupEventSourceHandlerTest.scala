package io.github.positionpal.group

import akka.Done
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.pattern.StatusReply.{Error, Success}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import io.github.positionpal.client.ClientADT.{ClientStatus, OutputReference}
import io.github.positionpal.client.{ClientID, CommunicationProtocol, Information, NewMessage}
import io.github.positionpal.group.ErrorValues.*
import io.github.positionpal.group.GroupEvent as Event
import io.github.positionpal.group.GroupEventSourceHandler.{Command, State}
import io.github.positionpal.group.InformationValues.*
import io.github.positionpal.message.ChatMessageADT
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupEventSourceHandlerTest
    extends ScalaTestWithActorTestKit(
      EventSourcedBehaviorTestKit.config.withFallback(ConfigFactory.load("local-config.conf")),
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
      invalidRequest.reply should ===(Error(CLIENT_ALREADY_JOINED withClientId clientID))

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
      result.reply should ===(Error(CLIENT_DOESNT_BELONGS_TO_GROUP withClientId clientID))

    "allow a client to connect to the group after joining" in:
      val clientID = ClientID(value = "1a2b4")
      val communicationChannel = testKit.createTestProbe[CommunicationProtocol]().ref

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
          client.status should ===(ClientStatus.ONLINE)
        case Left(error) => fail(s"Expected client to exist, but got error: $error")

    "return an error when a client tries to connect without joining first" in:
      val clientID = ClientID(value = "1a2b4")
      val communicationChannel = testKit.createTestProbe[CommunicationProtocol]().ref

      val result = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID, communicationChannel, replyTo))

      result.reply.isError should ===(true)
      result.reply should ===(Error(CLIENT_DOESNT_BELONGS_TO_GROUP withClientId clientID))

      val state = eventSourcedBehaviorTestKit.getState()
      state.isPresent(clientID) should ===(false)

    "allow a client to reconnect with a different communication channel" in:
      val clientID = ClientID(value = "1a2b4")
      val firstChannel = testKit.createTestProbe[CommunicationProtocol]().ref
      val secondChannel = testKit.createTestProbe[CommunicationProtocol]().ref

      eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID, replyTo))
      eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID, firstChannel, replyTo))

      val stateAfterFirstConnect = eventSourcedBehaviorTestKit.getState()
      stateAfterFirstConnect.getClient(clientID) match
        case Right(client) =>
          client.outputRef should ===(OutputReference.OUT(firstChannel))
          client.status should ===(ClientStatus.ONLINE)
        case Left(error) => fail(s"Expected client to exist, but got error: $error")

      val result = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID, secondChannel, replyTo))

      result.reply.isSuccess should ===(true)
      result.reply should ===(Success(ClientSuccessfullyConnected(clientID)))

      val stateAfterReconnect = eventSourcedBehaviorTestKit.getState()
      stateAfterReconnect.getClient(clientID) match
        case Right(client) =>
          client.outputRef should ===(OutputReference.OUT(secondChannel))
          client.status should ===(ClientStatus.ONLINE)
        case Left(error) => fail(s"Expected client to exist, but got error: $error")

    "Group Event Source Handler broadcast mechanism" should:
      "broadcast a message when a client joins the group" in:
        val clientID1 = ClientID(value = "client1")
        val clientID2 = ClientID(value = "client2")

        val probe1 = testKit.createTestProbe[CommunicationProtocol]()
        val probe2 = testKit.createTestProbe[CommunicationProtocol]()

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID1, replyTo))
        eventSourcedBehaviorTestKit
          .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID1, probe1.ref, replyTo))

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID2, replyTo))
        eventSourcedBehaviorTestKit
          .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID2, probe2.ref, replyTo))

        val messageProbe1 = probe1.expectMessageType[Information]
        val messageProbe2 = probe2.expectMessageType[Information]

        messageProbe1.content should ===(CLIENT_CONNECTED.text)
        messageProbe2.content should ===(CLIENT_CONNECTED.text)

      "broadcast message when a client leaves the group" in:
        val clientID1 = ClientID(value = "client1")
        val clientID2 = ClientID(value = "client2")

        val probe1 = testKit.createTestProbe[CommunicationProtocol]()
        val probe2 = testKit.createTestProbe[CommunicationProtocol]()

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID1, replyTo))
        probe1.expectNoMessage()
        eventSourcedBehaviorTestKit
          .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID1, probe1.ref, replyTo))

        val probe1ConnectionMessage = probe1.expectMessageType[Information]
        probe1ConnectionMessage.content should ===(CLIENT_CONNECTED.text)

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID2, replyTo))
        val probe1JoiningMessage = probe1.expectMessageType[Information]
        probe1JoiningMessage.content should ===(CLIENT_JOINED.text)

        eventSourcedBehaviorTestKit
          .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID2, probe2.ref, replyTo))

        val probe1Client2Connection = probe1.expectMessageType[Information]
        val probe2Client2Connection = probe2.expectMessageType[Information]

        probe1Client2Connection.content should ===(CLIENT_CONNECTED.text)
        probe2Client2Connection.content should ===(CLIENT_CONNECTED.text)

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientLeavesGroup(clientID1, replyTo))

        val probe2ClientLeave = probe2.expectMessageType[Information]
        probe2ClientLeave.content should ===(CLIENT_LEAVED.text)

      "broadcast a message sent from a client" in:

        val clientID1 = ClientID(value = "client1")
        val clientID2 = ClientID(value = "client2")

        val probe1 = testKit.createTestProbe[CommunicationProtocol]()
        val probe2 = testKit.createTestProbe[CommunicationProtocol]()

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID1, replyTo))
        eventSourcedBehaviorTestKit
          .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID1, probe1.ref, replyTo))

        val probe1ConnectionMessage = probe1.expectMessageType[Information]
        probe1ConnectionMessage.content should ===(CLIENT_CONNECTED.text)

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientID2, replyTo))

        val probe1JoiningMessage = probe1.expectMessageType[Information]
        probe1JoiningMessage.content should ===(CLIENT_JOINED.text)

        eventSourcedBehaviorTestKit
          .runCommand[StatusReply[Reply]](replyTo => ClientConnects(clientID2, probe2.ref, replyTo))
        val probe1Client2Connection = probe1.expectMessageType[Information]
        val probe2Client2Connection = probe2.expectMessageType[Information]

        probe1Client2Connection.content should ===(CLIENT_CONNECTED.text)
        probe2Client2Connection.content should ===(CLIENT_CONNECTED.text)

        val textOfMessage = "This is actually a test"
        val messageToGroup = ChatMessageADT.now(textOfMessage, from = clientID1, to = "testGroup")

        eventSourcedBehaviorTestKit.runCommand[StatusReply[Done]](replyTo => SendMessage(messageToGroup, replyTo))

        val probe1ReceivedMsg = probe1.expectMessageType[NewMessage]
        val probe2ReceivedMsg = probe2.expectMessageType[NewMessage]

        probe1ReceivedMsg.content should ===(textOfMessage)
        probe1ReceivedMsg.content should ===(probe2ReceivedMsg.content)
