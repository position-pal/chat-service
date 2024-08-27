package messages

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.serialization.jackson.CborSerializable
import messages.Command.UserEnterToGroup
import messages.Event.{UserEnteredToGroup, UserLeavedFromGroup}

/** Input command for the Group entity */
enum Command extends CborSerializable:
  case UserEnterToGroup(user: String, replyTo: ActorRef[StatusReply[?]])
  case UserLeaveFromGroup(user: String, replyTo: ActorRef[StatusReply[?]])

/** Event triggered inside the Group entity */
enum Event:
  case UserEnteredToGroup(user: String)
  case UserLeavedFromGroup(user: String)

object Group:

  /** Current State maintained on the entity
    * @param users the users that are currently in the group
    */
  class State(users: Seq[String]) extends CborSerializable:
    def addUser(user: String) = State(user +: users)
    def removeUser(user: String) = State(users.filterNot(_ == user))

  private object State:
    def empty: State = State(Seq.empty)

  def entityKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Group")

  def apply(groupId: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId(entityKey.name, groupId),
      emptyState = State.empty,
      commandHandler = commandHandler,
      eventHandler = eventHandler,
    )

  /** Create a cluster shard for a Group entity
    * @param system The system used for initializing a shard
    */
  def init(system: ActorSystem[?]): Unit =
    ClusterSharding(system).init(
      Entity(entityKey)(entityContext => Group(entityContext.entityId)),
    )

  /** Handle an incoming command
    * @param state the current state of the entity
    * @param command the command that should be handled
    * @return
    */
  def commandHandler(state: State, command: Command): Effect[Event, State] =
    command match
      case UserEnterToGroup(user, _) => Effect.persist(UserEnteredToGroup(user))
      case messages.Command.UserLeaveFromGroup(user, _) => Effect.persist(UserLeavedFromGroup(user))

  /** Handle a triggered event
    *
    * @param state the current state of the entity
    * @param event the triggered event that should be handled
    * @return
    */
  def eventHandler(state: State, event: Event): State =
    event match
      case UserEnteredToGroup(user) => state.addUser(user)
      case UserLeavedFromGroup(user) => state.removeUser(user)
