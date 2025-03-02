package io.github.positionpal.grpc

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Future, TimeoutException}

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.pattern.AskTimeoutException
import akka.stream.ConnectionException
import com.typesafe.config.ConfigFactory
import io.github.positionpal.message.GroupMessageStorage
import io.github.positionpal.proto.StatusCode.{BAD_REQUEST, GENERIC_ERROR, OK, REQUEST_TIMEOUT, SERVICE_UNAVAILABLE}
import io.github.positionpal.proto.{ChatService, RetrieveLastMessagesRequest}
import io.github.positionpal.storage.MessageStorage
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ServiceHandlerTest extends AnyWordSpec with MockFactory with BeforeAndAfterAll with Matchers with ScalaFutures:

  private val conf = ConfigFactory.load("local-config.conf")
  private val testKit: ActorTestKit = ActorTestKit(conf)

  private val system: ActorSystem[Nothing] = testKit.system
  private val storage: MessageStorage[Future] = GroupMessageStorage(system)
  private val service: ChatService = ServiceHandler(system, storage)

  private val mockStorage: MessageStorage[Future] = mock[MessageStorage[Future]]
  private val mockService: ChatService = ServiceHandler(system, mockStorage)
  override def afterAll(): Unit =
    super.afterAll()
    testKit.shutdownTestKit()

  given patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))

  "ServiceHandler" should:
    "handle requests for message history" in:
      val request = RetrieveLastMessagesRequest("a123", "4")
      val reply = service.retrieveLastMessages(request)

      reply.futureValue.code should be(OK)
      reply.futureValue.messages.map(_.content.strip()) should be(
        Seq("Ok then, let's do this!", "It's getting late, I think", "See you then", "bye!"),
      )

    "handle request for non-existent group" in:
      val request = RetrieveLastMessagesRequest("non-existent", "2")
      val reply = service.retrieveLastMessages(request)

      reply.futureValue.code should be(OK)
      reply.futureValue.messages.length should be(0)

    "handle bad request errors" in:

      val request = RetrieveLastMessagesRequest("not-present", "2")
      (mockStorage.getLastMessages(_: String)(_: Int)).expects(request.groupId, request.numberOfMessages.toInt)
        .returning(
          Future.successful(Left(("Invalid group ID: non-existent", new IllegalArgumentException))),
        )

      val reply = mockService.retrieveLastMessages(request)

      reply.futureValue.code should be(BAD_REQUEST)
      reply.futureValue.messages.length should be(0)

    "handle service unavailable errors" in:

      val request = RetrieveLastMessagesRequest("a123", "4")
      (mockStorage.getLastMessages(_: String)(_: Int)).expects(request.groupId, request.numberOfMessages.toInt)
        .returning(
          Future.successful(Left(("Unable to connect to Cassandra", ConnectionException("")))),
        )

      val reply = mockService.retrieveLastMessages(request)

      reply.futureValue.code should be(SERVICE_UNAVAILABLE)
      reply.futureValue.messages.length should be(0)

    "handle timeout errors" in:

      val request = RetrieveLastMessagesRequest("a123", "4")
      (mockStorage.getLastMessages(_: String)(_: Int)).expects(request.groupId, request.numberOfMessages.toInt)
        .returning(
          Future.successful(Left(("Query timed out", AskTimeoutException("")))),
        )

      val reply = mockService.retrieveLastMessages(request)

      reply.futureValue.code should be(REQUEST_TIMEOUT)
      reply.futureValue.messages.length should be(0)

      (mockStorage.getLastMessages(_: String)(_: Int)).expects(request.groupId, request.numberOfMessages.toInt)
        .returning(
          Future.successful(Left(("Client timeout", new TimeoutException))),
        )

      val otherReply = mockService.retrieveLastMessages(request)

      otherReply.futureValue.code should be(REQUEST_TIMEOUT)
      otherReply.futureValue.messages.length should be(0)

    "handle generic errors" in:

      val request = RetrieveLastMessagesRequest("a123", "4")
      (mockStorage.getLastMessages(_: String)(_: Int)).expects(request.groupId, request.numberOfMessages.toInt)
        .returning(
          Future.successful(Left(("Unexpected error", new Exception))),
        )

      val reply = mockService.retrieveLastMessages(request)

      reply.futureValue.code should be(GENERIC_ERROR)
      reply.futureValue.messages.length should be(0)
