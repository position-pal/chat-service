package io.github.positionpal.client
import akka.serialization.jackson.CborSerializable

case class ClientID(value: String) extends CborSerializable
