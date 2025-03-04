package io.github.positionpal.client

import java.time.Instant

import io.github.positionpal.borer.BorerSerialization

sealed trait CommunicationProtocol extends BorerSerialization
case class NewMessage(group: String, from: ClientID, content: String, time: Instant) extends CommunicationProtocol
case class Information(content: String, clientID: ClientID, time: Instant) extends CommunicationProtocol

object Protocols:
  def message(group: String, from: ClientID, content: String, time: Instant = Instant.now()): CommunicationProtocol =
    NewMessage(group, from, content, time)

  def information(content: String, clientID: ClientID, time: Instant = Instant.now()): CommunicationProtocol =
    Information(content, clientID, time)
