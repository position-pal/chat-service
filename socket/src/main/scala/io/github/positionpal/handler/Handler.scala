package io.github.positionpal.handler

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import io.github.positionpal.message.ChatMessageADT.ChatMessage

/** Maintains the logic and protocols for managing the exchange of messages using websocket.
  * It handles both incoming and outgoing WebSocket messages, including stream lifecycle events.
  */
object Handler:
  enum Commands:
    /** Command representing an incoming WebSocket message containing text content.
      * @param content The text content of the incoming message.
      */
    case IncomingMessage(content: String)

    /** Command representing an outgoing WebSocket message.
      * @param content The outgoing chat message to be sent via WebSocket.
      */
    case OutgoingMessage(content: ChatMessage)

    /** Command signaling that the WebSocket stream has completed successfully. */
    case StreamCompletedSuccessfully

    /** Command signaling that the WebSocket stream completed with an exception.
      * @param ex The exception that caused the stream to fail.
      */
    case StreamCompletedWithException(ex: Throwable)

  import Commands.*

  /** Case class responsible for storing the handler for outgoing messages.
    * @param outgoingMessageHandler An ActorRef to handle outgoing WebSocket messages.
    */
  case class WebSocketHandler(outgoingMessageHandler: ActorRef[OutgoingMessage])

  /** Trait representing the connection handler for WebSocket communication.
    * It provides a behavior for processing WebSocket messages.
    * @tparam T The type of messages this handler will process.
    */
  private trait WebSocketConnectionHandler[T]:
    /** Defines the behavior for handling incoming WebSocket messages.
      * @return A Behavior for the given message type T.
      */
    def connectionHandler: Behavior[T]

  private object IncomingHandler extends WebSocketConnectionHandler[Commands]:
    /** The behavior for handling incoming WebSocket commands.
      * @return A Behavior for processing Commands.
      */
    override def connectionHandler: Behavior[Commands] = Behaviors.receive: (context, message) =>
      message match
        case IncomingMessage(text) =>
          // TODO: Here should be implemented the logic for handling incoming messages
          context.log.info(s"Processing incoming message: $text")
        case _ =>
          context.log.info("Can't process the following message")
      Behaviors.same

  /** Exposes the behavior for handling incoming WebSocket messages.
    * This method returns the behavior that will handle incoming WebSocket commands.
    * @return A Behavior for processing Commands.
    */
  def incomingHandler: Behavior[Commands] = IncomingHandler.connectionHandler
