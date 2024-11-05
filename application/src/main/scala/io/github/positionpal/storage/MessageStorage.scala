package io.github.positionpal.storage

trait MessageStorage:

  /** Retrieve the last n messages that where persisted inside a Group
    * @param groupID identifier of the group
    * @param n numer of messages that should be retrieved
    * @return a [[Seq]] with the last n messages of the group
    */
  def getLastMessages(groupID: String)(n: Int): Seq[String]
