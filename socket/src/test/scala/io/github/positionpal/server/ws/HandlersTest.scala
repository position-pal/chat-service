package io.github.positionpal.server.ws

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.github.positionpal.entity.WebSocketActor.Commands
import io.github.positionpal.entity.WebSocketActor.Commands.{IncomingMessage, OutgoingMessage}
import io.github.positionpal.server.ws.Handlers.websocketHandler
import io.github.positionpal.message.ChatMessageADT.{ChatMessage, message as chatMessage}

class HandlersTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers:

  "WebSocket Handler" should :
    "handle incoming messages correctly" in:

      val incomingProbe = TestProbe[Commands]()
      val handler = websocketHandler(incomingProbe.ref)

      val (testSource, _) = TestSource()
        .via(handler)
        .toMat(TestSink())(Keep.both)
        .run()

      testSource.sendNext(TextMessage.Strict("Hello WebSocket"))
      incomingProbe.expectMessage(IncomingMessage("Hello WebSocket"))

    "send outgoing messages correctly" in:
      val incomingProbe = TestProbe[Commands]()
      val handler = websocketHandler(incomingProbe.ref)

      val (webSocketHandler, testSink) = Source.empty[Message]
        .viaMat(handler)(Keep.right)
        .toMat(TestSink())(Keep.both)
        .run()

      testSink.request(1)

      val outgoingActorRef = webSocketHandler.outgoingMessageHandler

      val chatMsg = chatMessage(
        text = "Hola",
        timestamp = "123",
        from = "Me",
        to = "Test Group"
      )

      outgoingActorRef ! OutgoingMessage(chatMsg)
      testSink.expectNext(TextMessage.Strict("Hola"))
