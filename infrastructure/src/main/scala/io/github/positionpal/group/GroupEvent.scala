package io.github.positionpal.group

import akka.actor.typed.ActorRef
import io.github.positionpal.borer.BorerSerialization
import io.github.positionpal.client.ClientID

sealed trait GroupEvent extends BorerSerialization
case class ClientJoinedToGroup(clientID: ClientID) extends GroupEvent
case class ClientLeavedFromGroup(clientID: ClientID) extends GroupEvent
case class ClientConnected(clientID: ClientID, communicationChannel: ActorRef[String]) extends GroupEvent
case class ClientDisconnected(clientID: ClientID) extends GroupEvent
case class Message(text: String) extends GroupEvent
