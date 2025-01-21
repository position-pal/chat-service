package io.github.positionpal.storage

import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT.Message

import scala.concurrent.Future

trait MessageStorage:

  /** Retrieve the last n messages that where persisted inside a Group
    * @param groupID identifier of the group
    * @param n number of messages that should be retrieved
    * @return a [[Future]] with a [[Seq]] of the last n messages of the group
    */
  def getLastMessages(groupID: String)(n: Int): Future[Seq[Message[ClientID, String]]]
