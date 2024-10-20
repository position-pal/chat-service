package io.github.positionpal.group

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import akka.serialization.jackson.CborSerializable
import io.github.positionpal.client.ClientADT.ClientID
import io.github.positionpal.group.GroupADT.Group

object GroupEventSourceHandler:

  opaque type State = Group[String]

  sealed trait Command extends CborSerializable
  case class ClientJoinsGroup(clientID: ClientID, replyTo: ActorRef[StatusReply[Reply]]) extends Command

  sealed trait Event extends CborSerializable
  case class ClientJoinedToGroup(clientID: ClientID) extends Event

  sealed trait Reply extends CborSerializable
  case class UserSuccessfullyJoined(users: List[ClientID]) extends Reply

  def apply(groupId: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId(entityKey.name, groupId),
      emptyState = Group.empty(groupId),
      commandHandler = commandHandler,
      eventHandler = eventHandler,
    )

  /** Init the source handler */
  def init(using system: ActorSystem[?]): Unit =
    ClusterSharding(system).init:
      Entity(entityKey)(ctx => GroupEventSourceHandler(ctx.entityId))

  /** The [[EntityTypeKey]] for the source handler
    * @return an instance of [[EntityTypeKey]]
    */
  def entityKey: EntityTypeKey[Command] = EntityTypeKey[Command](getClass.getName)

  /** Handle an incoming command from the outside, triggering an event in the domain as response
    * @param state The actual state of the entity
    * @param command The received command
    * @return Return a [[ReplyEffect]] with the response of the operation
    */
  private def commandHandler(state: State, command: Command): ReplyEffect[Event, State] = command match
    case ClientJoinsGroup(clientID, replyTo) =>
      print(state)
      Effect.persist(ClientJoinedToGroup(clientID)).thenReply(replyTo): state =>
        StatusReply.Success(UserSuccessfullyJoined(state.clientIDList))

  /** Handle a triggered event letting the entity pass to a new state
    * @param state The actual state of the entity
    * @param event The triggered event
    * @return The new state of the entity
    */
  private def eventHandler(state: State, event: Event): State = event match
    case ClientJoinedToGroup(clientID) =>
      state.addClient(clientID, "a") match
        case Right(newState: State) => newState
        case _ => state
