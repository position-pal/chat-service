package io.github.positionpal.client

object ClientADT:

  enum ClientStatus:
    case ONLINE
    case OFFLINE

  enum OutputReference[+O]:
    case OUT(value: O)
    case EMPTY

  trait ClientOps[I]:
    /** The ID associated to the client
      * @return the [[I]] of the current [[Client]]
      */
    def id: I

    /** The [[OutputReference]] indicating the channel where we can
      * actually exchange a message with the [[Client]].
      * @return
      */
    def outputRef: OutputReference[?]

    /** Set a new [[OutputReference]] for the client
      * @return a new [[ClientOps]] instance with a new [[OutputReference]].
      */
    def setOutputRef[O](reference: OutputReference[O]): Client[I]

    /** The current status of a client
      * @return the [[ClientStatus]] representing the status of a [[Client]]
      */
    def status: ClientStatus

    /** Set a new status for the client
      * @param newStatus the new status
      * @return a new [[ClientOps]] instance with the new status
      */
    def setStatus(newStatus: ClientStatus): Client[I]

  import io.github.positionpal.client.ClientADT.OutputReference.EMPTY
  import io.github.positionpal.client.ClientADT.ClientStatus.OFFLINE

  case class Client[I](id: I, outputRef: OutputReference[?] = EMPTY, status: ClientStatus) extends ClientOps[I]:
    override def setStatus(newStatus: ClientStatus): Client[I] = Client(id, outputRef, newStatus)
    override def setOutputRef[O](newReference: OutputReference[O]): Client[I] = Client(id, newReference, status)

  object Client:
    def empty[I](id: I): Client[I] = Client(id, EMPTY, OFFLINE)
