package io.github.positionpal.handler

import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.util.ByteString
import io.github.positionpal.client.ClientID
import io.github.positionpal.service.GroupService
import io.github.positionpal.services.GroupHandlerService
import io.github.positionpal.{AvroSerializer, MessageType}

object Handlers:
  private val serializer = AvroSerializer()

  def basic(using actorSystem: ActorSystem[?]): MessageHandler[Future] =
    (messageType: MessageType, message: ByteString) =>
      val service: GroupHandlerService = GroupService(actorSystem)
      val byteArray = message.toArray

      messageType match
        case MessageType.GROUP_CREATED =>
          val deserializedEvent = serializer.deserializeGroupCreated(byteArray)

          val user = deserializedEvent.createdBy()
          val groupId = deserializedEvent.groupId()

          service.join(groupId)(ClientID(user.id()))

        case MessageType.GROUP_DELETED =>
          val deserializedEvent = serializer.deserializeGroupDeleted(byteArray)
          val groupId = deserializedEvent.groupId()

          service.delete(groupId)

        case MessageType.MEMBER_ADDED =>
          val deserializedEvent = serializer.deserializeAddedMemberToGroup(byteArray)

          val user = deserializedEvent.addedMember()
          val groupId = deserializedEvent.groupId()

          service.join(groupId)(ClientID(user.id()))

        case MessageType.MEMBER_REMOVED =>
          val deserializedEvent = serializer.deserializeRemovedMemberToGroup(byteArray)

          val user = deserializedEvent.removedMember()
          val groupId = deserializedEvent.groupId()

          service.leave(groupId)(ClientID(user.id()))
