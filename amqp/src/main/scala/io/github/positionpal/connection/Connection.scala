package io.github.positionpal.connection

import scala.util.Try

import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpUriConnectionProvider}
import cats.syntax.all.*
import io.github.positionpal.connection.Configuration.{RabbitMQConfig, Validation, ValidationResult}

enum ConnectionProviderError:
  case ConfigurationError(errors: List[Validation])
  case ConnectionFactoryError(cause: Throwable)

object Connection:

  import ConnectionProviderError.*
  private def createProvider(config: RabbitMQConfig): Either[ConnectionProviderError, AmqpConnectionProvider] =
    Try:
      AmqpUriConnectionProvider(config.toUri)
    .toEither.left.map(ConnectionFactoryError.apply)

  extension (result: ValidationResult[RabbitMQConfig])
    def toProvider: Either[ConnectionProviderError, AmqpConnectionProvider] =
      result.toEither.leftMap(errors => ConnectionProviderError.ConfigurationError(errors.toList))
        .flatMap(config => createProvider(config))
