package io.github.positionpal.connection

object AmqpConfiguration:
  /** Configuration settings for connecting to a RabbitMQ server.
    *
    * @param host        The hostname of the RabbitMQ server. Must be a valid hostname.
    * @param port        The port number of the RabbitMQ server. Must be in the range 0 to 65535.
    * @param virtualHost The virtual host to connect to. Defaults to an empty string.
    *                    If set, must be a valid virtual host name.
    * @param username    The username for authentication. Must be between 3 and 30 characters and can contain
    *                    alphanumeric characters, dots, underscores, and hyphens.
    * @param password    The password for authentication. Must contain printable ASCII characters only
    *                    and be up to 255 characters long.
    * @param ssl         Whether to use SSL for the connection. Defaults to `false`.
    */
  case class RabbitMQConfig(
      host: String,
      port: Int,
      virtualHost: String = "",
      username: String,
      password: String,
      ssl: Boolean = false,
  )

  extension (config: RabbitMQConfig)
    /** Converts the RabbitMQ configuration into a URI string suitable for connection.
      *
      * @return A URI string of the format:
      *         `amqp[s]://username:password@host:port[/encodedVirtualHost]`.
      *         The scheme will be `amqps` if SSL is enabled, otherwise `amqp`.
      *         The virtual host will be URL-encoded if specified and not `/`.
      */
    def toUri: String =
      val scheme = if config.ssl then "amqps" else "amqp"
      val encodedVHost =
        if config.virtualHost.isEmpty || config.virtualHost == "/" then ""
        else s"/${java.net.URLEncoder.encode(config.virtualHost, "UTF-8")}"
      s"$scheme://${config.username}:${config.password}@${config.host}:${config.port}$encodedVHost"

  enum Validation:
    /** Represents a validation error for a specific field in the RabbitMQ configuration.
      *
      * @param field The name of the field that is invalid.
      * @param msg   A descriptive error message for the invalid field.
      */
    case InvalidField(field: String, msg: String)

  import Validation.*
  import cats.data.*
  import cats.data.Validated.*
  import cats.syntax.all.*

  type ValidationResult[A] = ValidatedNec[Validation, A]

  /** Validates a value based on a given condition.
    *
    * @param value     The value to validate.
    * @param condition A predicate function to check the validity of the value.
    * @param onError   The validation error to return if the condition fails.
    * @return A [[ValidationResult]] containing the value if valid, or an error otherwise.
    */
  private def validate[A](value: A, condition: A => Boolean, onError: Validation): ValidationResult[A] =
    if condition(value) then value.validNec else onError.invalidNec

  /** Validates the `host` field.
    *
    * @param host The hostname to validate.
    * @return A [[ValidationResult]] containing the hostname if valid, or an error otherwise.
    */
  private def validateHost(host: String): ValidationResult[String] =
    validate(
      host,
      _.matches("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$"),
      InvalidField("host", "Invalid host specified"),
    )

  /** Validates the `port` field.
    *
    * @param port The port number to validate.
    * @return A [[ValidationResult]] containing the port number if valid, or an error otherwise.
    */
  private def validatePort(port: Int): ValidationResult[Int] =
    validate(
      port,
      port => port >= 0 && port <= 65535,
      InvalidField("port", "Port is not in the right range"),
    )

  /** Validates the `virtualHost` field.
    *
    * @param virtualHost The virtual host name to validate.
    * @return A [[ValidationResult]] containing the virtual host if valid, or an error otherwise.
    */
  private def validateVirtualHost(virtualHost: String): ValidationResult[String] =
    validate(
      virtualHost,
      _.matches("^(\\/|[a-zA-Z0-9-_.\\/]+)$"),
      InvalidField("virtualHost", "Virtual Host name is not valid"),
    )

  /** Validates the `username` field.
    *
    * @param username The username to validate.
    * @return A [[ValidationResult]] containing the username if valid, or an error otherwise.
    */
  private def validateUsername(username: String): ValidationResult[String] =
    validate(
      username,
      _.matches("^[a-zA-Z0-9._-]{3,30}$"),
      InvalidField("username", "Username is not valid"),
    )

  /** Validates the `password` field.
    *
    * @param password The password to validate.
    * @return A [[ValidationResult]] containing the password if valid, or an error otherwise.
    */
  private def validatePassword(password: String): ValidationResult[String] =
    validate(
      password,
      _.matches("^[\\x21-\\x7E]{1,255}$"),
      InvalidField("password", "Password is not valid"),
    )

  /** Constructs a validated RabbitMQ configuration object.
    *
    * @param host        The hostname of the RabbitMQ server.
    * @param port        The port number of the RabbitMQ server.
    * @param virtualHost The virtual host to connect to.
    * @param username    The username for authentication.
    * @param password    The password for authentication.
    * @param ssl         Whether to use SSL for the connection. Defaults to `false`.
    * @return A [[ValidationResult]] containing a `RabbitMQConfig` object if all fields are valid,
    *         or a list of validation errors otherwise.
    */
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
