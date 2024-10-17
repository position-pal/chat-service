package io.github.positionpal.message

import io.github.positionpal.client.ClientADT.ClientID

object ChatMessageADT:

  type ChatMessage = ChatMessageImpl

  /** Represents the data that a ChatMessage exposes */
  trait MessageOps:
    def text: String
    def timestamp: String
    def from: ClientID

  case class ChatMessageImpl(text: String, timestamp: String, from: ClientID) extends MessageOps

  /** Create a new chat message.
    *
    * @param text The content of the message
    * @param timestamp The timestamp of the message
    * @param from The ClientID that sent the message
    *
    * @return a [[ChatMessage]] instance
    */
  def message(text: String, timestamp: String, from: ClientID): ChatMessage =
    ChatMessageImpl(text, timestamp, from)
