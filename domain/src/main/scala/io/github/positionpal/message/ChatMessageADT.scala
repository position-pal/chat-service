package io.github.positionpal.message

object ChatMessageADT:

  type ChatMessage[I, T] = ChatMessageImpl[I, T]

  /** Represents the data that a ChatMessage exposes */
  trait MessageOps[I, T]:
    def text: String
    def timestamp: String
    def from: I
    def to: T

  case class ChatMessageImpl[I, T](text: String, timestamp: String, from: I, to: T) extends MessageOps[I, T]

  /** Create a new chat message.
    * @param text The content of the message
    * @param timestamp The timestamp of the message
    * @param from The ClientID that sent the message
    * @return a [[ChatMessage]] instance
    */
  def message[I, T](text: String, timestamp: String, from: I, to: T): ChatMessage[I, T] =
    ChatMessageImpl(text, timestamp, from, to)
