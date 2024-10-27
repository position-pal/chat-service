package io.github.positionpal.reply

import akka.serialization.jackson.CborSerializable
import io.github.positionpal.client.ClientID

sealed trait Reply extends CborSerializable
case class ClientSuccessfullyJoined(users: List[ClientID]) extends Reply
case class ClientSuccessfullyLeaved(clientID: ClientID) extends Reply
