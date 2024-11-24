package io.github.positionpal.connection

import cats.data.Reader
import io.github.positionpal.connection.Configuration.RabbitMQConfig

object Connection:
  type RabbitMQConnectionReader[A] = Reader[RabbitMQConfig, A]
