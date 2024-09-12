trait ReactionADT:

  /** Represent something that has occurred inside the system and that should be handled by the Reaction. */
  type Event

  /** Represent the state of an object that could change while handling the execution of the [[Event]]. */
  type State

  /** The context of a Reaction
    * @param event The occurred event
    * @param state The current state of the entity that reacts to the event.
    */
  case class Context(event: Event, state: State)
