package io.github.positionpal.handler

import akka.util.ByteString
import io.github.positionpal.MessageType

trait MessageHandler[F[_]]:
  def handle[A](messageType: MessageType, message: ByteString): F[A]
