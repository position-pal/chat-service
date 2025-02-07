package io.github.positionpal.storage

import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT.Message

trait MessageStorage[F[_]]:

  /** Retrieve the last n messages that where persisted inside a Group
    * @param groupID identifier of the group
    * @param n number of messages that should be retrieved
    * @return a [[F]] with a [[Seq]] of the last n messages of the group
    */

  type ErrorDescription = (String, Error | Throwable)
  type MessageList = Seq[Message[ClientID, String]]

  def getLastMessages(groupID: String)(n: Int): F[Either[ErrorDescription, MessageList]]
