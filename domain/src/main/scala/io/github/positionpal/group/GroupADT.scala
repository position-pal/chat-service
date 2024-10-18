package io.github.positionpal.group

import io.github.positionpal.client.ClientADT.ClientID

object GroupADT:

  enum ErrorCode:
    case ClientAlreadyPresent(clientID: ClientID)

  trait GroupOps[O]:
    /** Add a new client inside a [[GroupOps]]
      * @param clientID the reference of the client that joins group
      * @return the updated group
      */
    def addClient(clientID: ClientID, outputRef: O): Either[ErrorCode, GroupOps[O]]

    /** List of clients IDs that are inside the group
      * @return A list of clients that are inside the group
      */
    def clientIDList: List[ClientID]

    /** Execute an action on the output reference for a client
      * @param action The function that must be executed for each client
      */
    def executeOnClients(action: O => Unit): Unit

  import ErrorCode.*

  case class Group[O](clients: Map[ClientID, O], name: String) extends GroupOps[O]:
    override def addClient(clientID: ClientID, outputRef: O): Either[ErrorCode, GroupOps[O]] =
      if clients isDefinedAt clientID then Left(ClientAlreadyPresent(clientID))
      else Right(Group(clients + (clientID -> outputRef), name))

    override def clientIDList: List[ClientID] = clients.keys.toList
    override def executeOnClients(action: O => Unit): Unit = clients.values.foreach(action)

  object Group:
    /** Return a group without clients inside of it
      * @param name the name of the group you're creating
      * @return A new [[Group]]
      */
    def empty[O](name: String): Group[O] = Group(Map.empty, name)
