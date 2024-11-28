package io.github.positionpal.handler

import akka.util.ByteString
import cats.Id
import io.github.positionpal.{AvroSerializer, MessageType}

object Handlers:
  private val serializer = AvroSerializer()

  def basic: MessageHandler[Id] = new MessageHandler[Id]:
    def handle[A](messageType: MessageType, message: ByteString): Id[A] =
      val byteArray = message.toArray

      messageType match
        case MessageType.GROUP_CREATED =>
          val result = serializer.deserializeGroupCreated(byteArray)
          println(result)
          result.asInstanceOf[A]

        case MessageType.GROUP_DELETED =>
          val result = serializer.deserializeGroupDeleted(byteArray)
          println(result)
          result.asInstanceOf[A]

        case MessageType.MEMBER_ADDED =>
          val result = serializer.deserializeAddedMemberToGroup(byteArray)
          println(result)
          result.asInstanceOf[A]

        case MessageType.MEMBER_REMOVED =>
          val result = serializer.deserializeRemovedMemberToGroup(byteArray)
          println(result)
          result.asInstanceOf[A]
