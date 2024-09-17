package reaction

import scala.reflect.ClassTag

object ReactiveEntityADT:

  trait ReactiveEntityADT[E, R <: ReactionADT]:
    /** Register a reaction that this entity should perform on a specific event
      * @tparam A The occurred event
      * @param reaction The reaction that should be executed
      * @return A [[ReactiveEntityADT]] which is capable to react on [[A]]
      */
    def register[A <: E: ClassTag](reaction: R): ReactiveEntityADT[E, R]

    /** Remove the reaction on a specific event
      * @tparam A The event that should be no longer tracked
      * @return A [[ReactiveEntityADT]] that doesn't react on the [[event]]
      */
    def remove[A <: E: ClassTag]: ReactiveEntityADT[E, R]

    /** Handle an incoming event
      * @param event The event that has been triggered in the system and that should be handled
      *              by the entity
      * @tparam A    [[ReactionResult]] type
      * @return      The result of the reaction associated to the occurred event
      */
    def handle[A <: Any](event: E): A
