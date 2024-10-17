package io.github.positionpal.group

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.serialization.jackson.CborSerializable

/** Reply object for the external requests */
case class Reply(users: Seq[String]) extends CborSerializable

/** Input command for the Group entity */
sealed trait Command extends CborSerializable
case class UserEnterToGroup(user: String, replyTo: ActorRef[StatusReply[Reply]]) extends Command
case class UserLeaveFromGroup(user: String, replyTo: ActorRef[StatusReply[Reply]]) extends Command

/** Event triggered inside the Group entity */
sealed trait Event extends CborSerializable
case class UserEnteredToGroup(user: String) extends Event
case class UserLeavedFromGroup(user: String) extends Event

object Group:

  /** Current State maintained on the entity
    * @param users the users that are currently in the group
    */
  class State(val users: Seq[String]) extends CborSerializable:
    def addUser(user: String) = State(user +: users)
    def removeUser(user: String) = State(users.filterNot(_ == user))
    def toReply: Reply = Reply(users)

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
      case UserEnterToGroup(user, replyTo) =>
        Effect.persist(UserEnteredToGroup(user)).thenReply(replyTo)(state => StatusReply.success(state.toReply))
      case UserLeaveFromGroup(user, replyTo) =>
        Effect.persist(UserLeavedFromGroup(user)).thenReply(replyTo)(state => StatusReply.success(state.toReply))

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
