package io.github.positionpal.handler

import akka.util.ByteString
import io.github.positionpal.MessageType

trait MessageHandler:
  def handle(messageType: MessageType, message: ByteString): Unit
