package io.github.positionpal.handler

import scala.concurrent.Future

import akka.util.ByteString
import io.github.positionpal.client.ClientCommunications.CommunicationProtocol
import io.github.positionpal.client.ClientID
import io.github.positionpal.services.GroupHandlerService
import io.github.positionpal.{AvroSerializer, MessageType}
import org.slf4j.LoggerFactory

object Handlers:
  private val serializer = AvroSerializer()
  private val logger = LoggerFactory.getLogger(getClass.getName)

  def basic(service: GroupHandlerService[Future, CommunicationProtocol]): MessageHandler[Future] =
    (messageType: MessageType, message: ByteString) =>
      val byteArray = message.toArray

      logger.debug(s"Received a message of type: $messageType")

      messageType match
        case MessageType.GROUP_CREATED =>
          val deserializedEvent = serializer.deserializeGroupCreated(byteArray)

          val user = deserializedEvent.createdBy()
          val groupId = deserializedEvent.groupId()

          logger.debug(s"Deserialized $user and GroupID $groupId")
          service.join(groupId)(ClientID(user.id()))

        case MessageType.GROUP_DELETED =>
          logger.debug(s"Received a message of type: $messageType")
          val deserializedEvent = serializer.deserializeGroupDeleted(byteArray)
          logger.debug(s"Deserialized event: $deserializedEvent")

          val groupId = deserializedEvent.groupId()
          logger.debug(s"Deserialized GroupID $groupId")
          service.delete(groupId)

        case MessageType.MEMBER_ADDED =>
          logger.debug(s"Received a message of type: $messageType")
          val deserializedEvent = serializer.deserializeAddedMemberToGroup(byteArray)
          logger.debug(s"Deserialized event: $deserializedEvent")

          val user = deserializedEvent.addedMember()
          val groupId = deserializedEvent.groupId()

          logger.debug(s"Deserialized $user and GroupID $groupId")
          service.join(groupId)(ClientID(user.id()))

        case MessageType.MEMBER_REMOVED =>
          val deserializedEvent = serializer.deserializeRemovedMemberToGroup(byteArray)
          logger.debug(s"Deserialized event: $deserializedEvent")

          val user = deserializedEvent.removedMember()
          val groupId = deserializedEvent.groupId()

          logger.debug(s"Deserialized $user and GroupID $groupId")
          service.leave(groupId)(ClientID(user.id()))
