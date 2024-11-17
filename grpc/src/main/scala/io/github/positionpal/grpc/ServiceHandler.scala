package io.github.positionpal.grpc

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.typed.ActorSystem
import io.github.positionpal.message.GroupMessageStorage
import io.github.positionpal.proto.{ChatService, Message, MessageResponse, RetrieveLastMessagesRequest}
import io.github.positionpal.storage.MessageStorage

class ServiceHandler(using system: ActorSystem[?]) extends ChatService:

  given ec: ExecutionContext = system.executionContext
  val storage: MessageStorage = GroupMessageStorage()

  override def retrieveLastMessages(in: RetrieveLastMessagesRequest): Future[MessageResponse] =
    for
      messages <- storage.getLastMessages(in.groupId)(in.numberOfMessages.toInt)
      transformed = messages.map(Message.apply(_))
    yield MessageResponse(transformed)
