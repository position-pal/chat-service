package io.github.positionpal.client


object ClientADT:

  /**
   * Represent a unique ID that's associated to a [[Client]]
   *
   * @param id the Id of the client
   * @param email the email associated to the client
   */
  case class ClientID(id: String, email: String)

  enum ClientStatus:
    case ONLINE
    case OFFLINE

  trait ClientOps:
    /**
     * The ID associated to the client
     * @return the [[ClientID]] of the current [[Client]]
     */
    def clientID: ClientID

    /**
     * The current status of a client
     * @return the [[ClientStatus]] representing the status of a [[Client]]
     */
    def status: ClientStatus

    /**
     * Set a new status for the client
     * @param newStatus the new status
     * @return a new [[ClientOps]] instance with the new status
     */
    def setStatus(newStatus: ClientStatus): ClientOps


  case class Client(clientID: ClientID, status: ClientStatus) extends ClientOps:
    override def setStatus(newStatus: ClientStatus): ClientOps = Client(clientID, newStatus)




