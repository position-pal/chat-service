package io.github.positionpal.message


object ChatMessageADT:

  opaque type ChatMessage = ChatMessageImpl

  /**
   * Represents the data that a ChatMessage exposes
   */
  trait MessageOps:
    def text: String
    def timestamp: String
    def from: String            // TODO: Change return type to User
    def to: String              // TODO: Change return type to Group


  private case class ChatMessageImpl(text: String, timestamp: String, from: String, to: String) extends MessageOps

  /**
   * Create a new chat message.
   *
   * @param text The content of the message
   * @param timestamp The timestamp of the message
   * @param from The user that sent the message
   * @param to The group where message should be delivered
   *           
   * @return a [[ChatMessage]] instance
   */
  def message(text: String, timestamp: String, from: String, to: String) : ChatMessage =
    ChatMessageImpl(text, timestamp, from, to)
    
    