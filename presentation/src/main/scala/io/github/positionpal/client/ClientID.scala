package io.github.positionpal.client
import akka.serialization.jackson.CborSerializable

case class ClientID(id: String) extends CborSerializable
