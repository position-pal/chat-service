package io.github.positionpal.grpc

import scala.concurrent.{ExecutionContext, Future, TimeoutException}

import akka.actor.typed.ActorSystem
import akka.pattern.AskTimeoutException
import akka.stream.ConnectionException
import io.github.positionpal.proto.StatusCode.*
import io.github.positionpal.proto.{ChatService, MessageResponse, RetrieveLastMessagesRequest}
import io.github.positionpal.storage.MessageStorage

case class ServiceHandler(system: ActorSystem[?], storage: MessageStorage[Future]) extends ChatService:

  given ec: ExecutionContext = system.executionContext

  import Conversions.messageProtoConversion
  override def retrieveLastMessages(in: RetrieveLastMessagesRequest): Future[MessageResponse] =
    storage.getLastMessages(in.groupId)(in.numberOfMessages.toInt).flatMap:
      case Right(messages) =>
        Future.successful(MessageResponse(OK, messages.map(messageProtoConversion)))

      case Left((_, _: IllegalArgumentException)) =>
        Future.successful(MessageResponse(BAD_REQUEST, Seq.empty))

      case Left((_, _: ConnectionException)) =>
        Future.successful(MessageResponse(SERVICE_UNAVAILABLE, Seq.empty))

      case Left((_, _: TimeoutException | _: AskTimeoutException)) =>
        Future.successful(MessageResponse(REQUEST_TIMEOUT, Seq.empty))

      case _ => Future.successful(MessageResponse(GENERIC_ERROR, Seq.empty))
