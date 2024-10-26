package io.github.positionpal.server.ws

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.github.positionpal.handler.Handler.Commands
import io.github.positionpal.handler.Handler.Commands.IncomingMessage
import io.github.positionpal.server.ws.WebSocketHandlers.websocketHandler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HandlersTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers:

  "WebSocket Handler" should:
    "handle incoming messages correctly" in:

      val incomingProbe = TestProbe[Commands]()
      val handler = websocketHandler(incomingProbe.ref)

      val (testSource, _) = TestSource().via(handler).toMat(TestSink())(Keep.both).run()

      testSource.sendNext(TextMessage.Strict("Hello WebSocket"))
      incomingProbe.expectMessage(IncomingMessage("Hello WebSocket"))
