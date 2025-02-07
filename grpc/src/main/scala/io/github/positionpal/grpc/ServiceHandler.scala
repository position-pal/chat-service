package io.github.positionpal.grpc

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import io.github.positionpal.message.GroupMessageStorage
import io.github.positionpal.proto.StatusCode.{ERROR, OK}
import io.github.positionpal.proto.{ChatService, MessageResponse, RetrieveLastMessagesRequest}

class ServiceHandler(using system: ActorSystem[?]) extends ChatService:

  given ec: ExecutionContext = system.executionContext
  val storage = GroupMessageStorage()

  import Conversions.given
  import scala.util.{Success, Failure}

  override def retrieveLastMessages(in: RetrieveLastMessagesRequest): Future[MessageResponse] =
    storage.getLastMessages(in.groupId)(in.numberOfMessages.toInt).transformWith:
      case Success(messages) =>
        Future.successful(MessageResponse(OK, messages.map(messageProtoConversion)))
      case Failure(_) =>
        Future.successful(MessageResponse(ERROR, Seq.empty))
