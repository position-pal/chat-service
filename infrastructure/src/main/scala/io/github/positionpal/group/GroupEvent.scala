package io.github.positionpal.group

import akka.actor.typed.ActorRef
import io.github.positionpal.borer.BorerSerialization
import io.github.positionpal.client.ClientID

sealed trait GroupEvent extends BorerSerialization

/** Event triggered when a client joins to the group.
  * @param clientID the reference of the client
  */
case class ClientJoinedToGroup(clientID: ClientID) extends GroupEvent

/** Event triggered when a client leaves from the group.
  * @param clientID the reference of the client
  */
case class ClientLeavedFromGroup(clientID: ClientID) extends GroupEvent

/** Event triggered when a client connects to the group
  * @param clientID the reference of the client
  * @param communicationChannel The communication channel of the client
  */
case class ClientConnected(clientID: ClientID, communicationChannel: ActorRef[String]) extends GroupEvent

/** Event triggered when a client connects to the group
  * @param clientID the reference of the client
  */
case class ClientDisconnected(clientID: ClientID) extends GroupEvent
case class Message(text: String) extends GroupEvent
