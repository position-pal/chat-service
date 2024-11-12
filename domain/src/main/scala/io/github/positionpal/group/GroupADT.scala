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

    /** Executes a function on a client if it exists in the group
      * @param id The client identifier
      * @param ex The function that is executed if the client exists
      * @tparam R The expected return value type
      * @return An [[Either]] object with an error occurred or the result of the function
      */
    def executeOnClient[R](id: I)(ex: O => R): Either[ErrorCode[I], R]

    /** Execute an action on a client if it belongs to the group and a specific condition is met
      * @param id The client identifier
      * @param condition A function that takes a client as input and return a Boolean
      * @param ok The function that is executed if the condition returns true
      * @param ko The function that is executed if the condition returns false
      * @tparam R The expected return value type
      * @return An [[Either]] object with an error occurred or the result of the function
      */
    def executeOnCondition[R](id: I)(condition: O => Boolean)(ok: O => R, ko: O => R): Either[ErrorCode[I], R]

  case class Group[I, O](clients: Map[I, O], name: String) extends GroupOps[I, O]:

    import ErrorCode.*

    override def addClient(clientID: I, outputRef: O): Either[ErrorCode[I], GroupOps[I, O]] =
      if isPresent(clientID) then Left(ClientAlreadyPresent(clientID))
      else Right(Group(clients + (clientID -> outputRef), name))

    override def getClient(clientID: I): Either[ErrorCode[I], O] =
      if !isPresent(clientID) then Left(ClientDoesntExists(clientID))
      else Right(clients(clientID))

    override def updateClient(clientID: I)(update: O => O): Either[ErrorCode[I], GroupOps[I, O]] =
      if !isPresent(clientID) then Left(ClientDoesntExists(clientID))
      else Right(Group(clients.updated(clientID, update(clients(clientID))), name))

    override def removeClient(clientID: I): Either[ErrorCode[I], GroupOps[I, O]] =
      if !isPresent(clientID) then Left(ClientDoesntExists(clientID))
      else Right(Group(clients - clientID, name))

    override def isPresent(clientID: I): Boolean = clients isDefinedAt clientID

    override def clientIDList: List[I] = clients.keys.toList

    override def executeOnClients(action: O => Unit): Unit = clients.values.foreach(action)

    override def executeOnClient[R](id: I)(ex: O => R): Either[ErrorCode[I], R] =
      if !isPresent(id) then Left(ClientDoesntExists(id))
      else Right(ex(clients(id)))

    override def executeOnCondition[R](
        id: I,
    )(condition: O => Boolean)(ok: O => R, ko: O => R): Either[ErrorCode[I], R] =
      if !isPresent(id) then Left(ClientDoesntExists(id))
      else
        val client = clients(id)
        if condition(client) then Right(ok(client))
        else Right(ko(client))

  object Group:
    /** Return a group without clients inside of it
      * @param name the name of the group you're creating
      * @return A new [[Group]]
      */
    def empty[I, O](name: String): Group[I, O] = Group(Map.empty, name)
