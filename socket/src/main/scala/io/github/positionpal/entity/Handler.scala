package io.github.positionpal.entity

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import io.github.positionpal.message.ChatMessageADT.ChatMessage

object Handler:
  enum Commands:
    case IncomingMessage(content: String)
    case OutgoingMessage(content: ChatMessage)

    case StreamCompletedSuccessfully
    case StreamCompletedWithException(ex: Throwable)

  import Commands.*

  case class WebSocketHandler(outgoingMessageHandler: ActorRef[OutgoingMessage])

  private trait WebSocketConnectionHandler[T]:
    def connectionHandler: Behavior[T]

  private object IncomingHandler extends WebSocketConnectionHandler[Commands]:
    override def connectionHandler: Behavior[Commands] = Behaviors.receive:
      (context, message) =>
        message match
          case IncomingMessage(text) =>
            // TODO: Here should be implemented the logic for handling incoming messages
            context.log.info(s"Processing incoming message: $text")
          case _ =>
            context.log.info(s"Can't process the following message")
        Behaviors.same

  def incomingHandler: Behavior[Commands] = IncomingHandler.connectionHandler









