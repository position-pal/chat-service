package io.github.positionpal.group

import akka.serialization.jackson.CborSerializable
import io.github.positionpal.client.ClientID

sealed trait Reply extends CborSerializable

/** The response that is currently sent back when a new client joins the group
  * @param users is the [[List]] of [[ClientID]] that are currently connected to the group
  */
case class ClientSuccessfullyJoined(users: List[ClientID]) extends Reply

/** The response that is currently sent back when a client leaves the group
  * @param clientID the [[ClientID]] that leaves the group
  */
case class ClientSuccessfullyLeaved(clientID: ClientID) extends Reply

/** The response that is currently sent back when a client connects to the group
  * @param clientID the [[ClientID]] that connects into the group
  */
case class ClientSuccessfullyConnected(clientID: ClientID) extends Reply

/** The response that is currently sent back when a client disconnects from the group
  *  @param clientID the [[ClientID]] that connects into the group
  */
case class ClientSuccessfullyDisconnected(clientID: ClientID) extends Reply
