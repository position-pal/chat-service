package reaction

import cats.Monad
import cats.implicits.toFlatMapOps

/** A data type used for representing a reaction to a given event */
trait ReactionADT:

  /** Represent something that has occurred inside the system and that should be handled by the Reaction. */
  type Event

  /** Represent the state of an object that could change while handling the execution of the [[Event]]. */
  type State

  /** Represent the result of the reaction */
  type ReactionResult

  /** The context of a Reaction
    * @param event The occurred event
    * @param state The current state of the entity that reacts to the event.
    */
  case class Context(event: Event, state: State)

  import cats.data.ReaderT

  /** Represent the reaction that's triggered within a specific [[Context]]
    * @tparam F The effect to apply
    */
  opaque type Reaction[F[_]] = ReaderT[F, Context, ReactionResult]

  /** Create a reaction on an given [[Context]]
    * @param reaction The function representing the reaction.
    * @return The [[Reaction]] object.
    */
  def on[F[_]](reaction: (context: Context) => F[ReactionResult]): Reaction[F] = ReaderT(reaction)

  extension [F[_]: Monad](reaction: Reaction[F])
    /** Executes the [[Reaction]] producing a [[ReactionResult]]
      * @param context The [[Context]] in which the [[Reaction]] should run
      * @return a functional structure `F` in which the result is contained
      */
    def apply(context: Context): F[ReactionResult] = reaction.run(context)

/** A set of operations that compose the execution of two or more [[Reaction]] together */
trait ComposableReaction extends ReactionADT:
  extension [F[_]: Monad](reaction: Reaction[F])
    /** Compose the execution of two [[Reaction]] executing this first and then the [[other]] provided
      * @param other The [[Reaction]] to execute after this one
      * @return The new [[Reaction]] that is the combination of the other two
      */
    def andThen(other: Reaction[F]): Reaction[F]

/** A set of operations that filter the [[Reaction]] execution */
trait FilterableReaction extends ReactionADT:
  extension [F[_]: Monad](reaction: Reaction[F])
    /** Execute the reaction only if a condition on the [[Context]] is met.
      * @param predicate A function that takes the [[Context]] and returns `true` if the reaction should be executed.
      * @return A new [[Reaction]] that only runs if the predicate is satisfied.
      */
    def filter(predicate: Context => Boolean): Reaction[F]

/** A specialized version of [[ReactionADT]] where the result of the reaction is wrapped in an `Option`.
  * This trait provides composition and filtering for reactions that return an `Option[T]`.
  *
  * @tparam T The type of the result wrapped in the `Option`.
  */
trait OptionReaction[T] extends ComposableReaction with FilterableReaction:

  override type ReactionResult = Option[T]

  extension [F[_]: Monad](reaction: Reaction[F])
    def andThen(other: Reaction[F]): Reaction[F] = on: context =>
      reaction(context).flatMap:
        case Some(_) => other(context)
        case _ => Monad[F].pure(None)

    def filter(predicate: Context => Boolean): Reaction[F] = on: context =>
      if predicate(context) then reaction(context) else Monad[F].pure(None)