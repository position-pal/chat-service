package io.github.positionpal.group

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.github.positionpal.client.{ClientID, ClientStatusHandler}
import io.github.positionpal.group.GroupADT.Group

object GroupEventSourceHandler:

  type State = Group[ClientID, ClientStatusHandler]
  type Event = GroupEvent
  type Command = GroupCommand

  def apply(groupId: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId(entityKey.name, groupId),
      emptyState = Group.empty(groupId),
      commandHandler = commandHandler,
      eventHandler = eventHandler,
    )

  /** The [[EntityTypeKey]] for the source handler
    * @return an instance of [[EntityTypeKey]]
    */
  def entityKey: EntityTypeKey[Command] = EntityTypeKey[Command](getClass.getName)

  /** Handle an incoming command from the outside, triggering an event in the domain as response
    * @param state The actual state of the entity
    * @param command The received command
    * @return Return a [[ReplyEffect]] with the response of the operation
    */
  private def commandHandler(state: State, command: Command): Effect[Event, State] = command match
    case ClientJoinsGroup(clientID, replyTo) =>
      if state.isPresent(clientID) then Effect.reply(replyTo)(StatusReply.Error(s"client $clientID already joined"))
      else
        Effect.persist(ClientJoinedToGroup(clientID)).thenReply(replyTo): state =>
          StatusReply.Success(ClientSuccessfullyJoined(state.clientIDList))

    case ClientLeavesGroup(clientID, replyTo) =>
      if !state.isPresent(clientID) then
        Effect.reply(replyTo)(StatusReply.Error(s"client $clientID doesn't belongs to the group"))
      else
        Effect.persist(ClientLeavedFromGroup(clientID)).thenReply(replyTo): state =>
          StatusReply.Success(ClientSuccessfullyLeaved(clientID))

  /** Handle a triggered event letting the entity pass to a new state
    * @param state The actual state of the entity
    * @param event The triggered event
    * @return The new state of the entity
    */
  private def eventHandler(state: State, event: Event): State = event match
    case ClientJoinedToGroup(clientID: ClientID) =>
      val emptyClient = ClientStatusHandler.empty(clientID)
      state.addClient(clientID, emptyClient) match
        case Right(newState: State) => newState
        case _ => state

    case ClientLeavedFromGroup(clientID: ClientID) =>
      state.removeClient(clientID) match
        case Right(newState: State) => newState
        case _ => state
