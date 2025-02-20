package io.github.positionpal.client

import akka.actor.typed.ActorRef
import io.github.positionpal.client.ClientADT.{Client, ClientStatus, OutputReference}

case class ClientStatusHandler(
    id: ClientID,
    outputRef: OutputReference[ActorRef[CommunicationProtocol]],
    status: ClientStatus,
) extends Client[ClientID, ActorRef[CommunicationProtocol]]:
  override def setOutputRef(
      reference: OutputReference[ActorRef[CommunicationProtocol]],
  ): Client[ClientID, ActorRef[CommunicationProtocol]] =
    ClientStatusHandler(id, reference, status)
  override def setStatus(newStatus: ClientStatus): Client[ClientID, ActorRef[CommunicationProtocol]] =
    ClientStatusHandler(id, outputRef, newStatus)

object ClientStatusHandler:
  def empty(id: ClientID): ClientStatusHandler = ClientStatusHandler(id, OutputReference.EMPTY, ClientStatus.OFFLINE)
