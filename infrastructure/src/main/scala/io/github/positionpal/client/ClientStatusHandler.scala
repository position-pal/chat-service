package io.github.positionpal.client

import akka.actor.typed.ActorRef
import io.github.positionpal.client.ClientADT.{Client, ClientStatus, OutputReference}

case class ClientStatusHandler(
    id: ClientID,
    outputRef: OutputReference[ActorRef[String]],
    status: ClientStatus,
) extends Client[ClientID, ActorRef[String]]:
  override def setOutputRef(reference: OutputReference[ActorRef[String]]): Client[ClientID, ActorRef[String]] =
    ClientStatusHandler(id, reference, status)
  override def setStatus(newStatus: ClientStatus): Client[ClientID, ActorRef[String]] =
    ClientStatusHandler(id, outputRef, newStatus)

object ClientStatusHandler:
  def empty(id: ClientID): ClientStatusHandler = ClientStatusHandler(id, OutputReference.EMPTY, ClientStatus.OFFLINE)
