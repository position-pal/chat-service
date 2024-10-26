package io.github.positionpal.event

import akka.serialization.jackson.CborSerializable
import io.github.positionpal.client.ClientID

sealed trait GroupEvent extends CborSerializable
case class ClientJoinedToGroup(clientID: ClientID) extends GroupEvent
case class ClientLeavedFromGroup(clientID: ClientID) extends GroupEvent
