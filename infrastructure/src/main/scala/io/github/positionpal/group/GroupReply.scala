package io.github.positionpal.group

import akka.serialization.jackson.CborSerializable
import io.github.positionpal.client.ClientID

sealed trait Reply extends CborSerializable
case class ClientSuccessfullyJoined(users: List[ClientID]) extends Reply
case class ClientSuccessfullyLeaved(clientID: ClientID) extends Reply
case class ClientSuccessfullyConnected(clientID: ClientID) extends Reply
case class ClientSuccessfullyDisconnected(clientID: ClientID) extends Reply
