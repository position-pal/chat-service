package io.github.positionpal.connection

object Configuration:

  case class RabbitMQConfig(
      host: String,
      port: Int,
      virtualHost: String = "",
      username: String,
      password: String,
      ssl: Boolean = false,
  )

  enum Validation:
    case InvalidField(field: String, msg: String)

  import Validation.*
  import cats.data.*
  import cats.data.Validated.*
  import cats.syntax.all.*

  type ValidationResult[A] = ValidatedNec[Validation, A]

  private def validate[A](value: A, condition: A => Boolean, onError: Validation): ValidationResult[A] =
    if condition(value) then value.validNec else onError.invalidNec

  private def validateHost(host: String): ValidationResult[String] =
    validate(
      host,
      _.matches("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$"),
      InvalidField("host", "Invalid host specified"),
    )

  private def validatePort(port: Int): ValidationResult[Int] =
    validate(
      port,
      port => port >= 0 && port <= 65535,
      InvalidField("port", "Port is not in the right range"),
    )

  private def validateVirtualHost(virtualHost: String): ValidationResult[String] =
    validate(
      virtualHost,
      _.matches("^(\\/|[a-zA-Z0-9-_.\\/]+)$"),
      InvalidField("virtualHost", "Virtual Host name is not valid"),
    )

  private def validateUsername(username: String): ValidationResult[String] =
    validate(
      username,
      _.matches("^[a-zA-Z0-9._-]{3,30}$"),
      InvalidField("username", "Username is not valid"),
    )

  private def validatePassword(password: String): ValidationResult[String] =
    validate(
      password,
      _.matches("^[\\x21-\\x7E]{1,255}$"),
      InvalidField("password", "Password is not valid"),
    )

  def of(
      host: String,
      port: Int,
      virtualHost: String,
      username: String,
      password: String,
      ssl: Boolean = false,
  ): ValidationResult[RabbitMQConfig] =
    (
      validateHost(host),
      validatePort(port),
      validateVirtualHost(virtualHost),
      validateUsername(username),
      validatePassword(password),
      Validated.validNec(ssl),
    ).mapN(RabbitMQConfig.apply)
