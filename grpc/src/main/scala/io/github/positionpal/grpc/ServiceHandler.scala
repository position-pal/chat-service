package io.github.positionpal.grpc

import scala.concurrent.Future

import io.github.positionpal.proto.{ChatService, Message, MessageResponse, RetrieveLastMessagesRequest}

class ServiceHandler extends ChatService:

  override def retrieveLastMessages(in: RetrieveLastMessagesRequest): Future[MessageResponse] =
    Future.successful(MessageResponse(Seq(Message("test"), Message("another_test"), Message("final_test"))))
