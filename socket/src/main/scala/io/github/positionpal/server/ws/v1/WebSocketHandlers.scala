package io.github.positionpal.server.ws.v1

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.RouteResult.Complete
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.ActorSource
import io.bullet.borer.Json
import io.github.positionpal.client.ClientCommunications.CommunicationProtocol
import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT
import io.github.positionpal.serializer.CommunicationSerializersImporter.given
import io.github.positionpal.services.GroupHandlerService

/** Object that contains the Flow handlers for websocket connections. */
object WebSocketHandlers:

  /** Create a [[Flow]] used for communicating messages between a client and a group
    * @param clientID The identifier of the client that is connecting
    * @param groupID The identifier of the group in which we should connect
    * @param ec  The implicit execution context
    * @param service The implicit service that handles the requests
    * @return A flow used for handling websoscket connections.
    */
  def connect(
      clientID: ClientID,
      groupID: String,
      service: GroupHandlerService[Future, CommunicationProtocol],
  )(using ec: ExecutionContext): Flow[Message, Message, ?] =

    val toGroup: Sink[Message, Unit] = Flow[Message].collect:
      case TextMessage.Strict(msg) => msg
    .map: text =>
      ChatMessageADT.now(text, clientID, groupID)
    .watchTermination(): (_, watcher) =>
      watcher.onComplete: _ =>
        service.disconnect(groupID)(clientID)
    .to:
      Sink.foreach(message => service.message(groupID)(message))

    val toClient: Source[Message, ActorRef[CommunicationProtocol]] = ActorSource.actorRef(
      completionMatcher = { case Complete => },
      failureMatcher = { case ex: Throwable => ex },
      bufferSize = 1000,
      overflowStrategy = OverflowStrategy.fail,
    ).mapMaterializedValue: ref =>
      service.connect(groupID)(clientID, ref)
      ref
    .map:
      case text: CommunicationProtocol => TextMessage(Json.encode(text).toUtf8String)

    Flow.fromSinkAndSource(toGroup, toClient)
