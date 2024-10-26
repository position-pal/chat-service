package io.github.positionpal.client

object ClientADT:

  enum ClientStatus:
    case ONLINE
    case OFFLINE

  trait ClientOps[I]:
    /** The ID associated to the client
      * @return the [[I]] of the current [[Client]]
      */
    def id: I

    /** The current status of a client
      * @return the [[ClientStatus]] representing the status of a [[Client]]
      */
    def status: ClientStatus

    /** Set a new status for the client
      * @param newStatus the new status
      * @return a new [[ClientOps]] instance with the new status
      */
    def setStatus(newStatus: ClientStatus): ClientOps[I]

  case class Client[I](id: I, status: ClientStatus) extends ClientOps[I]:
    override def setStatus(newStatus: ClientStatus): ClientOps[I] = Client(id, newStatus)
