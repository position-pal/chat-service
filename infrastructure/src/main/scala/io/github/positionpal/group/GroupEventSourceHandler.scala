package io.github.positionpal.group

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.github.positionpal.client.ClientADT.ClientStatus.*
import io.github.positionpal.client.ClientADT.OutputReference.*
import io.github.positionpal.client.{ClientID, ClientStatusHandler}
import io.github.positionpal.group.GroupADT.{Group, GroupOps}
import io.github.positionpal.group.GroupDSL.*
import io.github.positionpal.message.ChatMessageADT.ChatMessageImpl

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

  import io.github.positionpal.group.ErrorValues.*
  import io.github.positionpal.group.InformationValues.*

  /** Handle an incoming command from the outside, triggering an event in the domain as response
    * @param state The actual state of the entity
    * @param command The received command
    * @return Return a [[ReplyEffect]] with the response of the operation
    */
  private def commandHandler(state: State, command: Command): Effect[Event, State] = command match
    case ClientJoinsGroup(clientID, replyTo) =>
      if state.isPresent(clientID) then
        Effect.reply(replyTo):
          StatusReply.Error(CLIENT_ALREADY_JOINED withClientId clientID)
      else
        Effect.persist(ClientJoinedToGroup(clientID)).thenReply(replyTo): state =>
          StatusReply.Success(ClientSuccessfullyJoined(state.clientIDList))

    case ClientLeavesGroup(clientID, replyTo) =>
      if !state.isPresent(clientID) then
        Effect.reply(replyTo):
          StatusReply.Error(CLIENT_DOESNT_BELONGS_TO_GROUP withClientId clientID)
      else
        Effect.persist(ClientLeavedFromGroup(clientID)).thenReply(replyTo): _ =>
          StatusReply.Success(ClientSuccessfullyLeaved(clientID))

    case ClientConnects(clientID, communicationChannel, replyTo) =>
      if !state.isPresent(clientID) then
        Effect.reply(replyTo):
          StatusReply.Error(CLIENT_DOESNT_BELONGS_TO_GROUP withClientId clientID)
      else
        Effect.persist(ClientConnected(clientID, communicationChannel)).thenReply(replyTo): _ =>
          StatusReply.Success(ClientSuccessfullyConnected(clientID))

    case ClientDisconnects(clientID, replyTo) =>
      if !state.isPresent(clientID) then
        Effect.reply(replyTo):
          StatusReply.Error(CLIENT_DOESNT_BELONGS_TO_GROUP withClientId clientID)
      else
        Effect.persist(ClientDisconnected(clientID)).thenReply(replyTo): _ =>
          StatusReply.Success(ClientSuccessfullyDisconnected(clientID))

    case SendMessage(ChatMessageImpl(text, _, _, _), replyTo) =>
      Effect.persist(Message(text)).thenReply(replyTo): _ =>
        StatusReply.Ack

  /** Handle a triggered event letting the entity pass to a new state
    * @param state The actual state of the entity
    * @param event The triggered event
    * @return The new state of the entity
    */
  private def eventHandler(state: State, event: Event): State = event match
    case ClientJoinedToGroup(clientID: ClientID) =>
      val emptyClient = ClientStatusHandler.empty(clientID)
      state.addClient(clientID, emptyClient) match
        case Right(newState: State) =>
          newState broadcast (CLIENT_JOINED withClientId clientID)
          newState
        case _ => state

    case ClientLeavedFromGroup(clientID: ClientID) =>
      state.removeClient(clientID) match
        case Right(newState: State) =>
          newState broadcast (CLIENT_LEAVED withClientId clientID)
          newState
        case _ => state

    case ClientConnected(clientID, communicationChannel) =>
      val updatedClient = state.updateClient(clientID):
        _.setOutputRef(OUT(communicationChannel)).setStatus(ONLINE).asInstanceOf[ClientStatusHandler]

      updatedClient match
        case Right(newState: State) =>
          newState broadcast (CLIENT_CONNECTED withClientId clientID)
          newState
        case _ => state

    case ClientDisconnected(clientID) =>
      val updatedClient = state.updateClient(clientID):
        _.setOutputRef(EMPTY).setStatus(OFFLINE).asInstanceOf[ClientStatusHandler]

      updatedClient match
        case Right(newState: State) =>
          newState broadcast (CLIENT_DISCONNECTED withClientId clientID)
          newState
        case _ => state

    case Message(text: String) =>
      state broadcast text
      state

  extension (group: Group[ClientID, ClientStatusHandler])
    /** Broadcast a message to the online members of the group
      * @param message the message to broadcast
      */
    private infix def broadcast(message: String): Unit =
      group.|>> { client =>
        if client.status == ONLINE then
          client.executeOnOutput: out =>
            out ! message
      }
