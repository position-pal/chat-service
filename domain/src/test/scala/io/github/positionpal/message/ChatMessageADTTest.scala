package io.github.positionpal.message

import java.time.Instant
import java.util.UUID

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ChatMessageADTTest extends AnyWordSpecLike with Matchers:

  type UserId = UUID
  type GroupId = String

  "ChatMessageADT.message" should:
    "create a message with given parameters" in:
      val userId = UUID.randomUUID()
      val groupId = "test-group"
      val text = "Hello, world!"
      val timestamp = Instant.now()

      val message = ChatMessageADT.message[UserId, GroupId](
        text = text,
        timestamp = timestamp,
        from = userId,
        to = groupId,
      )

      message.text shouldBe text
      message.timestamp shouldBe timestamp
      message.from shouldBe userId
      message.to shouldBe groupId

  "ChatMessageADT.now" should:
    "create a message with current timestamp" in:
      val userId = UUID.randomUUID()
      val groupId = "test-group"
      val text = "Hello, world!"

      val message = ChatMessageADT.now[UserId, GroupId](
        text = text,
        from = userId,
        to = groupId,
      )

      message.text shouldBe text
      message.from shouldBe userId
      message.to shouldBe groupId
      message.timestamp.isBefore(Instant.now()) shouldBe true

  "ChatMessageADT" should:
    "support different identifier types" in:

      val intMessage = ChatMessageADT.message[Int, String](
        text = "Integer user",
        timestamp = Instant.now(),
        from = 42,
        to = "group-a",
      )

      val stringMessage = ChatMessageADT.message[String, Long](
        text = "String user",
        timestamp = Instant.now(),
        from = "user-x",
        to = 123L,
      )

      intMessage.from shouldBe 42
      intMessage.to shouldBe "group-a"
      stringMessage.from shouldBe "user-x"
      stringMessage.to shouldBe 123L

    "demonstrate immutability" in:
      val userId = UUID.randomUUID()
      val groupId = "test-group"
      val originalText = "Original message"
      val timestamp = Instant.now()

      val message = ChatMessageADT.message[UserId, GroupId](
        text = originalText,
        timestamp = timestamp,
        from = userId,
        to = groupId,
      )

      val newMessage = ChatMessageADT.message[UserId, GroupId](
        text = "Modified message",
        timestamp = timestamp,
        from = userId,
        to = groupId,
      )

      message.text shouldBe originalText
      newMessage.text shouldBe "Modified message"
      message should not be theSameInstanceAs(newMessage)

    "create a timestamp very close to now" in:
      val message = ChatMessageADT.now[UserId, GroupId](
        text = "Timestamp test",
        from = UUID.randomUUID(),
        to = "test-group",
      )

      val now = Instant.now()
      val timeDifference = now.toEpochMilli - message.timestamp.toEpochMilli

      timeDifference should be < 1000L
