package io.github.positionpal.connection

import io.github.positionpal.connection.AmqpConfiguration.Validation.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmqpConfigurationTest extends AnyWordSpec with Matchers:

  "RabbitMQ Configuration" should:
    "accept valid configuration" in:
      val result = AmqpConfiguration.of(
        host = "rabbitmq.example.com",
        port = 5672,
        virtualHost = "my-vhost",
        username = "user123",
        password = "password123!",
      )

      result.isValid shouldBe true
      result.toOption.map: config =>
        config.host shouldBe "rabbitmq.example.com"
        config.port shouldBe 5672
        config.virtualHost shouldBe "my-vhost"
        config.username shouldBe "user123"
        config.password shouldBe "password123!"
        config.ssl shouldBe false

    "validate host" when:
      "accepting valid hosts" in:
        val validHosts = List(
          "localhost",
          "example.com",
          "sub1.sub2.example.com",
          "my-host-name",
          "host123",
        )

        validHosts.foreach: host =>
          val result = AmqpConfiguration.of(
            host = host,
            port = 5672,
            virtualHost = "/",
            username = "user123",
            password = "password123!",
          )

          withClue(s"Host '$host' should be valid: "):
            result.isValid shouldBe true

      "rejecting invalid hosts" in:
        val invalidHosts = List(
          "",
          "host@invalid",
          "host:with:colons",
          "host with spaces",
          "host#invalid",
        )

        invalidHosts.foreach: host =>
          val result = AmqpConfiguration.of(
            host = host,
            port = 5672,
            virtualHost = "/",
            username = "user123",
            password = "password123!",
          )

          withClue(s"Host '$host' should be invalid: "):
            result.isInvalid shouldBe true
            result.fold(
              errors =>
                errors.exists {
                  case InvalidField("host", _) => true
                  case _ => false
                } shouldBe true,
              _ => fail("Expected validation to fail"),
            )

    "validate port" when:
      "accepting valid ports" in:
        val validPorts = List(0, 1024, 5672, 65535)

        validPorts.foreach: port =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = port,
            virtualHost = "/",
            username = "user123",
            password = "password123!",
          )

          withClue(s"Port $port should be valid: "):
            result.isValid shouldBe true

      "rejecting invalid ports" in:
        val invalidPorts = List(-1, -5672, 65536, 70000)

        invalidPorts.foreach: port =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = port,
            virtualHost = "/",
            username = "user123",
            password = "password123!",
          )

          withClue(s"Port $port should be invalid: "):
            result.isInvalid shouldBe true
            result.fold(
              errors =>
                errors.exists {
                  case InvalidField("port", _) => true
                  case _ => false
                } shouldBe true,
              _ => fail("Expected validation to fail"),
            )

    "validate virtual host" when:
      "accepting valid virtual hosts" in:
        val validVHosts = List(
          "/",
          "my_vhost",
          "my-vhost",
          "vhost.name",
          "vhost/nested",
        )

        validVHosts.foreach: vhost =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = 5672,
            virtualHost = vhost,
            username = "user123",
            password = "password123!",
          )
          withClue(s"Virtual host '$vhost' should be valid: "):
            result.isValid shouldBe true

      "rejecting invalid virtual hosts" in:
        val invalidVHosts = List(
          "vhost@invalid",
          "vhost with spaces",
          "vhost#invalid",
        )

        invalidVHosts.foreach: vhost =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = 5672,
            virtualHost = vhost,
            username = "user123",
            password = "password123!",
          )
          withClue(s"Virtual host '$vhost' should be invalid: "):
            result.isInvalid shouldBe true
            result.fold(
              errors =>
                errors.exists {
                  case InvalidField("virtualHost", _) => true
                  case _ => false
                } shouldBe true,
              _ => fail("Expected validation to fail"),
            )

    "validate username" when:
      "accepting valid usernames" in:
        val validUsernames = List(
          "user123",
          "my.user",
          "my_user",
          "my-user",
          "abc123",
        )

        validUsernames.foreach: username =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = 5672,
            virtualHost = "/",
            username = username,
            password = "password123!",
          )
          withClue(s"Username '$username' should be valid: "):
            result.isValid shouldBe true

      "rejecting invalid usernames" in:
        val invalidUsernames = List(
          "ab", // too short
          "a" * 31, // too long
          "user@invalid",
          "user with spaces",
          "user#invalid",
        )

        invalidUsernames.foreach: username =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = 5672,
            virtualHost = "/",
            username = username,
            password = "password123!",
          )
          withClue(s"Username '$username' should be invalid: "):
            result.isInvalid shouldBe true
            result.fold(
              errors =>
                errors.exists {
                  case InvalidField("username", _) => true
                  case _ => false
                } shouldBe true,
              _ => fail("Expected validation to fail"),
            )

    "validate password" when:
      "accepting valid passwords" in:
        val validPasswords = List(
          "password123!",
          "myP@ssw0rd",
          "simple",
          "!@#$%^&*()",
          "a" * 255, // max length
        )

        validPasswords.foreach: password =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = 5672,
            virtualHost = "/",
            username = "user123",
            password = password,
          )
          withClue("Password should be valid: "):
            result.isValid shouldBe true

      "rejecting invalid passwords" in:
        val invalidPasswords = List(
          "", // empty
          "a" * 256, // too long
          "password\n", // invalid character
          "password\t", // invalid character
          "password ", // space is not in the valid range
        )

        invalidPasswords.foreach: password =>
          val result = AmqpConfiguration.of(
            host = "localhost",
            port = 5672,
            virtualHost = "/",
            username = "user123",
            password = password,
          )
          withClue("Password should be invalid: "):
            result.isInvalid shouldBe true
            result.fold(
              errors =>
                errors.exists {
                  case InvalidField("password", _) => true
                  case _ => false
                } shouldBe true,
              _ => fail("Expected validation to fail"),
            )

      "accumulate multiple validation errors" in:
        val result = AmqpConfiguration.of(
          host = "invalid@host",
          port = -1,
          virtualHost = "invalid vhost",
          username = "in", // too short
          password = "", // empty
        )

        result.isInvalid shouldBe true

        result.fold(
          errors =>
            val errorFields = errors.iterator.map:
              case InvalidField(field, _) =>
                field
            .toSet
            errorFields should have size 5
            errorFields should contain allOf ("host", "port", "virtualHost", "username", "password")
          ,
          _ => fail("Expected validation to fail"),
        )
