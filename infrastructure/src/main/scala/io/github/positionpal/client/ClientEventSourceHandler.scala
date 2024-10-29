package io.github.positionpal.client

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.github.positionpal.client.ClientADT.Client
import io.github.positionpal.client.ClientADT.OutputReference.OUT
import io.github.positionpal.command.{ClientCommand, ReceiveReference}
import io.github.positionpal.event.{ClientEvent, NewReference}

object ClientEventSourceHandler:

  type State = Client[String]
  type Event = ClientEvent
  type Command = ClientCommand

  def apply(clientId: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId(entityKey.name, clientId),
      emptyState = Client.empty(clientId),
      commandHandler = commandHandler,
      eventHandler = eventHandler,
    )

  /** The [[EntityTypeKey]] for the source handler
    * @return an instance of [[EntityTypeKey]]
    */
  def entityKey: EntityTypeKey[Command] = EntityTypeKey[Command](getClass.getName)

  private def commandHandler(state: State, command: Command): Effect[Event, State] = command match
    case ReceiveReference(ref: ActorRef[?]) =>
      println(state)
      Effect.persist(NewReference(ref))

  private def eventHandler(state: State, event: Event): State = event match
    case NewReference(ref) => state.setOutputRef(OUT(ref))
