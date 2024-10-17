package io.github.positionpal.group

import io.github.positionpal.client.ClientADT.Client

object GroupADT:

  trait GroupOps:
    def addClient(client: Client): GroupOps
    def clients: List[Client]

  case class Group(clients: List[Client], name: String) extends GroupOps:
    override def addClient(client: Client): GroupOps = Group(client :: clients, name)

  object Group:
    def empty(name: String): Group = Group(List.empty, name)
