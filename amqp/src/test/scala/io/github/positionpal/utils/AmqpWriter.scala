package io.github.positionpal.utils

import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.alpakka.amqp.scaladsl.AmqpFlow
import akka.stream.alpakka.amqp.{AmqpWriteSettings, ExchangeDeclaration, WriteMessage, WriteResult}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import com.rabbitmq.client.AMQP.BasicProperties
import io.github.positionpal.MessageType
import io.github.positionpal.connection.Configuration
import io.github.positionpal.connection.Connection.toProvider

object AmqpWriter:
  private val connectionConfiguration = Configuration.of(
    host = "localhost",
    port = 5672,
    virtualHost = "/",
    username = "guest",
    password = "admin",
  )

  def send(message: ByteString, msgType: MessageType, exchangeName: String, queue: String)(using
      ActorSystem[?],
  ): Future[WriteResult] =
    connectionConfiguration.toProvider match
      case Right(provider) =>
        val writeSettings = AmqpWriteSettings(provider).withRoutingKey(queue)
          .withDeclaration(ExchangeDeclaration(name = exchangeName, exchangeType = "headers"))

        val flow: Flow[WriteMessage, WriteResult, Future[Done]] = AmqpFlow(writeSettings)
        Source.single(message).map: el =>
          val headersMap = Map("message_type" -> msgType.name())
          val props = BasicProperties.Builder().headers(headersMap.asJava).build()
          WriteMessage(el).withProperties(props)
        .via(flow).runWith(Sink.last)

      case Left(value) =>
        Future.failed(InstantiationError(value.toString))
