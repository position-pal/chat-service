package io.github.positionpal.group

import java.time.Instant

import akka.actor.typed.ActorRef
import io.github.positionpal.borer.BorerSerialization
import io.github.positionpal.client.{ClientID, CommunicationProtocol}

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
case class ClientConnected(clientID: ClientID, communicationChannel: ActorRef[CommunicationProtocol]) extends GroupEvent

/** Event triggered when a client connects to the group
  * @param clientID the reference of the client
  */
case class ClientDisconnected(clientID: ClientID) extends GroupEvent

/** Event triggered when a client send a message in the group
  * @param from The message sender id
  * @param text The body of the message that is sent in the group
  * @param time The message timestamp
  */
case class Message(from: ClientID, text: String, time: Instant) extends GroupEvent
