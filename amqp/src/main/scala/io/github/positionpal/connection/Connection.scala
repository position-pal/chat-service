package io.github.positionpal.connection

import scala.util.Try

import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpUriConnectionProvider}
import cats.syntax.all.*
import io.github.positionpal.connection.Configuration.{RabbitMQConfig, Validation, ValidationResult}

enum ConnectionProviderError:
  /** Indicates a configuration error with a list of validation issues.
    *
    * @param errors A list of validation errors that occurred during configuration validation.
    */
  case ConfigurationError(errors: List[Validation])

  /** Indicates a failure to create the connection factory.
    *
    * @param cause The underlying exception that caused the failure.
    */
  case ConnectionFactoryError(cause: Throwable)

object Connection:
  import ConnectionProviderError.*

  /** Creates an AMQP connection provider from a valid RabbitMQ configuration.
    *
    * @param config The validated RabbitMQ configuration.
    * @return Either an [[AmqpConnectionProvider]] on success, or a [[ConnectionProviderError]] on failure.
    */
  private def createProvider(config: RabbitMQConfig): Either[ConnectionProviderError, AmqpConnectionProvider] =
    Try:
      AmqpUriConnectionProvider(config.toUri)
    .toEither.left.map(ConnectionFactoryError.apply)

  extension (result: ValidationResult[RabbitMQConfig])
    /** Converts a [[ValidationResult]] of a [[RabbitMQConfig]] into an [[Either]] containing an [[AmqpConnectionProvider]]
      * or a [[ConnectionProviderError]].
      *
      * @return [[Right]] containing the connection provider if the configuration is valid, or [[Left]]
      *         containing a [[ConnectionProviderError]] if validation or provider creation fails.
      */
    def toProvider: Either[ConnectionProviderError, AmqpConnectionProvider] =
      result.toEither.leftMap(errors => ConnectionProviderError.ConfigurationError(errors.toList))
        .flatMap(config => createProvider(config))
