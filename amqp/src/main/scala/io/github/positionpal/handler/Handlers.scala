package io.github.positionpal.handler

import akka.util.ByteString
import io.github.positionpal.{AvroSerializer, MessageType}

object Handlers:

  private val serializer = AvroSerializer()

  def basic: MessageHandler = (messageType: MessageType, message: ByteString) =>
    val byteArray = message.toArray

    messageType match
      case MessageType.GROUP_CREATED => println(serializer.deserializeGroupCreated(byteArray))
      case MessageType.GROUP_DELETED => println(serializer.deserializeGroupDeleted(byteArray))
      case MessageType.MEMBER_ADDED => println(serializer.deserializeAddedMemberToGroup(byteArray))
      case MessageType.MEMBER_REMOVED => println(serializer.deserializeRemovedMemberToGroup(byteArray))
