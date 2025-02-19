package io.github.positionpal.handler

import akka.util.ByteString
import io.github.positionpal.events.EventType

/** A trait for handling messages of various types within an effectful context.
  *
  * @tparam F The effect type in which the message handling operation is performed.
  */
trait MessageHandler[F[_]]:

  /** Handles a message of a specific type and processes it within the context of the specified effect type.
    *
    * @param messageType The type of the message being handled.
    * @param message     The raw message payload, represented as a [[ByteString]].
    * @return An effectful computation that processes the message and produces a result of type `A`.
    */
  def handle(messageType: EventType, message: ByteString): F[Any]
