package io.github.positionpal.grpc

import scala.concurrent.{ExecutionContext, Future, TimeoutException}

import akka.actor.typed.ActorSystem
import akka.pattern.AskTimeoutException
import akka.stream.ConnectionException
import io.github.positionpal.proto.StatusCode.*
import io.github.positionpal.proto.{ChatService, MessageResponse, RetrieveLastMessagesRequest, Status}
import io.github.positionpal.storage.MessageStorage

case class ServiceHandler(system: ActorSystem[?], storage: MessageStorage[Future]) extends ChatService:

  given ec: ExecutionContext = system.executionContext

  import Conversions.messageProtoConversion
  override def retrieveLastMessages(in: RetrieveLastMessagesRequest): Future[MessageResponse] =
    storage.getLastMessages(in.groupId)(in.numberOfMessages.toInt).flatMap:
      case Right(messages) =>
        Future.successful:
          MessageResponse(Some(Status(OK, "")), messages.map(messageProtoConversion))

      case Left((_, _: IllegalArgumentException)) =>
        Future.successful:
          MessageResponse(Some(Status(BAD_REQUEST, "Error while formatting the request")), Seq.empty)

      case Left((_, _: ConnectionException)) =>
        Future.successful:
          MessageResponse(Some(Status(SERVICE_UNAVAILABLE, "Can't obtain messages from database")), Seq.empty)

      case Left((_, _: TimeoutException | _: AskTimeoutException)) =>
        Future.successful:
          MessageResponse(Some(Status(REQUEST_TIMEOUT, "Timeout for request")), Seq.empty)

      case _ =>
        Future.successful:
          MessageResponse(Some(Status(GENERIC_ERROR, "Timeout for request")), Seq.empty)
