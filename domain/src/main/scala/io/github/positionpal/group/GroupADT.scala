package io.github.positionpal.group

import io.github.positionpal.client.ClientADT.ClientID

object GroupADT:

  enum ErrorCode:
    case ClientAlreadyPresent(clientID: ClientID)
    case ClientDoesntExists(clientID: ClientID)

  trait GroupOps[O]:
    /** Add a new client inside a [[GroupOps]]
      * @param clientID the reference of the client that joins group
      * @return the updated group
      */
    def addClient(clientID: ClientID, outputRef: O): Either[ErrorCode, GroupOps[O]]

    /** Remove a client inside a [[GroupOps]]
      * @param clientID the reference of the client to remove
      * @return
      */
    def removeClient(clientID: ClientID): Either[ErrorCode, GroupOps[O]]

    /** Check if a [[ClientID]] is already present inside the group
      * @param clientID The [[ClientID]] to search
      */
    def isPresent(clientID: ClientID): Boolean

    /** List of clients IDs that are inside the group
      * @return A list of clients that are inside the group
      */
    def clientIDList: List[ClientID]

    /** Execute an action on the output reference for a client
      * @param action The function that must be executed for each client
      */
    def executeOnClients(action: O => Unit): Unit

  import ErrorCode.*

  case class Group[O](private val _clients: Map[ClientID, O], name: String) extends GroupOps[O]:
    override def addClient(clientID: ClientID, outputRef: O): Either[ErrorCode, GroupOps[O]] =
      if _clients isDefinedAt clientID then Left(ClientAlreadyPresent(clientID))
      else Right(Group(_clients + (clientID -> outputRef), name))

    override def removeClient(clientID: ClientID): Either[ErrorCode, GroupOps[O]] =
      if !(_clients isDefinedAt clientID) then Left(ClientDoesntExists(clientID))
      else Right(Group(_clients - clientID, name))

    override def isPresent(clientID: ClientID): Boolean = _clients isDefinedAt clientID

    override def clientIDList: List[ClientID] = _clients.keys.toList

    override def executeOnClients(action: O => Unit): Unit = _clients.values.foreach(action)

  object Group:
    /** Return a group without clients inside of it
      * @param name the name of the group you're creating
      * @return A new [[Group]]
      */
    def empty[O](name: String): Group[O] = Group(Map.empty, name)
