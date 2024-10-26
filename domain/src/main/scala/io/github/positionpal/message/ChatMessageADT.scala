package io.github.positionpal.message

object ChatMessageADT:

  type ChatMessage[I] = ChatMessageImpl[I]

  /** Represents the data that a ChatMessage exposes */
  trait MessageOps[I]:
    def text: String
    def timestamp: String
    def from: I

  case class ChatMessageImpl[I](text: String, timestamp: String, from: I) extends MessageOps[I]

  /** Create a new chat message.
    *
    * @param text The content of the message
    * @param timestamp The timestamp of the message
    * @param from The ClientID that sent the message
    *
    * @return a [[ChatMessage]] instance
    */
  def message[I](text: String, timestamp: String, from: I): ChatMessage[I] =
    ChatMessageImpl(text, timestamp, from)
