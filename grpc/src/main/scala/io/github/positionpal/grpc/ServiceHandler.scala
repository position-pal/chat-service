package io.github.positionpal.grpc

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import io.github.positionpal.message.GroupMessageStorage
import io.github.positionpal.proto.{ChatService, MessageResponse, RetrieveLastMessagesRequest}
import io.github.positionpal.storage.MessageStorage

class ServiceHandler(using system: ActorSystem[?]) extends ChatService:

  given ec: ExecutionContext = system.executionContext
  val storage: MessageStorage = GroupMessageStorage()

  import Conversions.given

  override def retrieveLastMessages(in: RetrieveLastMessagesRequest): Future[MessageResponse] =
    for
      messages <- storage.getLastMessages(in.groupId)(in.numberOfMessages.toInt)
      transformed = messages.map(messageProtoConversion)
    yield MessageResponse(transformed)
