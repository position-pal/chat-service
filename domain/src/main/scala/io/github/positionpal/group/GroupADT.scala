package io.github.positionpal.group

object GroupADT:

  enum ErrorCode[I]:
    case ClientAlreadyPresent(clientID: I)
    case ClientDoesntExists(clientID: I)

  trait GroupOps[I, O]:

    /** Add a new client inside a [[GroupOps]]
      * @param clientID the reference of the client that joins group
      * @return the updated group
      */
    def addClient(clientID: I, outputRef: O): Either[ErrorCode[I], GroupOps[I, O]]

    /** Return a client reference that is currently inside the group
      * @param clientID the id of the client
      * @return An [[Either]] object indicating an error for the retrieval or the actual reference
      */
    def getClient(clientID: I): Either[ErrorCode[I], O]

    /** Update a client inside the group
      * @param clientID the id of the client
      * @param update the update function for the
      * @return
      */
    def updateClient(clientID: I)(update: O => O): Either[ErrorCode[I], GroupOps[I, O]]

    /** Remove a client inside a [[GroupOps]]
      * @param clientID the reference of the client to remove
      * @return An [[Either]] object indicating an error for the deletion or the updated Group
      */
    def removeClient(clientID: I): Either[ErrorCode[I], GroupOps[I, O]]

    /** Check if a [[ClientID]] is already present inside the group
      * @param clientID The [[ClientID]] to search
      */
    def isPresent(clientID: I): Boolean

    /** List of clients IDs that are inside the group
      * @return A list of clients that are inside the group
      */
    def clientIDList: List[I]

    /** Execute an action on the output reference for a client
      * @param action The function that must be executed for each client
      */
    def executeOnClients(action: O => Unit): Unit

  case class Group[I, O](clients: Map[I, O], name: String) extends GroupOps[I, O]:

    import ErrorCode.*

    override def addClient(clientID: I, outputRef: O): Either[ErrorCode[I], GroupOps[I, O]] =
      if clients isDefinedAt clientID then Left(ClientAlreadyPresent(clientID))
      else Right(Group(clients + (clientID -> outputRef), name))

    override def getClient(clientID: I): Either[ErrorCode[I], O] =
      if !(clients isDefinedAt clientID) then Left(ClientDoesntExists(clientID))
      else Right(clients(clientID))

    override def updateClient(clientID: I)(update: O => O): Either[ErrorCode[I], GroupOps[I, O]] =
      if !(clients isDefinedAt clientID) then Left(ClientDoesntExists(clientID))
      else Right(Group(clients.updated(clientID, update(clients(clientID))), name))

    override def removeClient(clientID: I): Either[ErrorCode[I], GroupOps[I, O]] =
      if !(clients isDefinedAt clientID) then Left(ClientDoesntExists(clientID))
      else Right(Group(clients - clientID, name))

    override def isPresent(clientID: I): Boolean = clients isDefinedAt clientID

    override def clientIDList: List[I] = clients.keys.toList

    override def executeOnClients(action: O => Unit): Unit = clients.values.foreach(action)

  object Group:
    /** Return a group without clients inside of it
      * @param name the name of the group you're creating
      * @return A new [[Group]]
      */
    def empty[I, O](name: String): Group[I, O] = Group(Map.empty, name)
