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

    /** Remove a client inside a [[GroupOps]]
      * @param clientID the reference of the client to remove
      * @return
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

  case class Group[I, O](private val _clients: Map[I, O], name: String) extends GroupOps[I, O]:

    import ErrorCode.*

    override def addClient(clientID: I, outputRef: O): Either[ErrorCode[I], GroupOps[I, O]] =
      if _clients isDefinedAt clientID then Left(ClientAlreadyPresent(clientID))
      else Right(Group(_clients + (clientID -> outputRef), name))

    override def removeClient(clientID: I): Either[ErrorCode[I], GroupOps[I, O]] =
      if !(_clients isDefinedAt clientID) then Left(ClientDoesntExists(clientID))
      else Right(Group(_clients - clientID, name))

    override def isPresent(clientID: I): Boolean = _clients isDefinedAt clientID

    override def clientIDList: List[I] = _clients.keys.toList

    override def executeOnClients(action: O => Unit): Unit = _clients.values.foreach(action)

  object Group:
    /** Return a group without clients inside of it
      * @param name the name of the group you're creating
      * @return A new [[Group]]
      */
    def empty[I, O](name: String): Group[I, O] = Group(Map.empty, name)
