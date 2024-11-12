package io.github.positionpal.client

object ClientADT:

  enum ClientStatus:
    case ONLINE
    case OFFLINE

  enum OutputReference[+O]:
    case OUT(value: O)
    case EMPTY

  trait Client[I, O]:
    /** The ID associated to the client
      *
      * @return the [[I]] of the current [[ClientStatus]]
      */
    def id: I

    /** The [[OutputReference]] indicating the channel where we can
      * actually exchange a message with the [[ClientStatus]].
      *
      * @return
      */
    def outputRef: OutputReference[O]

    /** Set a new [[OutputReference]] for the client
      * @return a new [[ClientOps]] instance with a new [[OutputReference]].
      */
    def setOutputRef(reference: OutputReference[O]): Client[I, O]

    /** The current status of a client
      *
      * @return the [[ClientStatus]] representing the status of a [[ClientStatus]]
      */
    def status: ClientStatus

    /** Set a new status for the client
      * @param newStatus the new status
      * @return a new [[ClientOps]] instance with the new status
      */
    def setStatus(newStatus: ClientStatus): Client[I, O]

    /** Perform an operation only if an output resource is set
      * @param f the operation to perform
      */
    def executeOnOutput(f: O => Unit): Unit =
      outputRef match
        case OutputReference.OUT(comm) => f(comm)
        case _ =>
