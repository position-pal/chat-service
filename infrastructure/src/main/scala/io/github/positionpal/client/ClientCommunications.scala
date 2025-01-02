package io.github.positionpal.client

import java.time.Instant

import io.github.positionpal.borer.BorerSerialization

object ClientCommunications:
  enum CommunicationType extends BorerSerialization:
    case INFO
    case MESSAGE

  import CommunicationType.*
  enum CommunicationProtocol(val replyType: CommunicationType) extends BorerSerialization:
    case NewMessage(group: String, from: ClientID, content: String, time: Instant)
        extends CommunicationProtocol(MESSAGE)
    case Information(content: String, time: Instant) extends CommunicationProtocol(INFO)

  import CommunicationProtocol.*

  def message(group: String, from: ClientID, content: String, time: Instant = Instant.now()): CommunicationProtocol =
    NewMessage(group, from, content, time)

  def information(content: String, time: Instant = Instant.now()): CommunicationProtocol = Information(content, time)
