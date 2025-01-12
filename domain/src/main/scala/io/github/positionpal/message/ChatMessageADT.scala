package io.github.positionpal.message

import java.time.Instant

object ChatMessageADT:

  /** Represents the data that a [[ChatMessage]] exposes */
  trait MessageOps[I, T]:
    /** The content of the message
      * @return a [[String]] with the message's content
      */
    def text: String

    /** The timestamp of the message
      * @return a [[String]] with the message's content
      */
    def timestamp: Instant

    /** The message's sender
      * @return the reference of the entity that sent the message
      */
    def from: I

    /** The message's receiving
      * @return the reference of the entity that receive the message
      */
    def to: T

  private case class ChatMessageImpl[I, T](text: String, timestamp: Instant, from: I, to: T) extends MessageOps[I, T]

  /** Return a new [[ChatMessage]]
    * @param text The content of the message
    * @param from The client that sent the message
    * @param to The group that received the message
    * @tparam I The identifier type of the client
    * @tparam T The identifier type of the group
    * @return a [[ChatMessage]] instance
    */
  def message[I, T](text: String, timestamp: Instant, from: I, to: T): MessageOps[I, T] =
    ChatMessageImpl(text, timestamp, from, to)

  /** Return a new [[ChatMessage]] with the timestamp set on the creation time
    * @param text The content of the message
    * @param from The client that sent the message
    * @param to The group that received the message
    * @tparam I The identifier type of the client
    * @tparam T The identifier type of the group
    * @return a [[ChatMessage]] instance
    */
  def now[I, T](text: String, from: I, to: T): MessageOps[I, T] = message(text, Instant.now(), from, to)
