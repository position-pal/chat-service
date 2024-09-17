package reaction

import scala.reflect.ClassTag

import reaction.Reaction.ReactionADT

object ReactiveEntity:

  /** Represents an entity that can register, remove, and handle reactions based on specific events.
    * @tparam E The type of events this entity can react to.
    * @tparam O The type of the result produced by handling an event (i.e., the [[ReactionResult]]).
    */
  trait ReactiveEntityOps[E, S, O]:
    /** Register a reaction that this entity should perform on a specific event
      *
      * @tparam A The occurred event
      * @param reaction The reaction that should be executed
      * @return A [[ReactiveEntityOps]] which is capable to react on [[A]]
      */
    def register[A <: E: ClassTag](reaction: ReactionADT[E, S, O]): ReactiveEntityOps[E, S, O]

    /** Remove the reaction on a specific event
      *
      * @tparam A The event that should be no longer tracked
      * @return A [[ReactiveEntityOps]] that doesn't react on the [[event]]
      */
    def remove[A <: E: ClassTag]: ReactiveEntityOps[E, S, O]

    /** Handle an incoming event
      * @param event The event that has been triggered in the system and that should be handled
      *              by the entity
      * @return      The result of the reaction associated to the occurred event
      */
    def handle(event: E): O
