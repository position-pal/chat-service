package io.github.positionpal.connection

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.stream.alpakka.amqp.scaladsl.AmqpSource
import akka.stream.alpakka.amqp.{AmqpUriConnectionProvider, NamedQueueSourceSettings, QueueDeclaration, ReadResult}
import akka.stream.scaladsl.{Flow, Sink, Source}
import io.github.positionpal.connection.Connection.*
import io.github.positionpal.consumer.QueueConsumer
import io.github.positionpal.consumer.QueueConsumer.Queue

@main
def executeTest(): Unit =

  given system: ActorSystem[None.type] = ActorSystem(Behaviors.empty, "test")

  val uri = "amqp://guest:admin@localhost:5672"
  val provider = AmqpUriConnectionProvider(uri)

  val queueName = "test"
  val queueDeclaration = QueueDeclaration(queueName)

  val amqpSource: Source[ReadResult, NotUsed] =
    AmqpSource.atMostOnceSource(
      NamedQueueSourceSettings(provider, "test").withDeclaration(queueDeclaration).withAckRequired(false),
      bufferSize = 10,
    )

  val messageFlow: Flow[ReadResult, Unit, NotUsed] =
    Flow[ReadResult].map: msg =>
      val messageBody = msg.bytes.utf8String
      println(s"Received message: $messageBody")

  val sink = Sink.ignore
  val stream = amqpSource.via(messageFlow).to(sink)

  stream.run()
  println(s"Listening for messages on RabbitMQ queue: $queueName")

@main
def graphTest(): Unit =

  given system: ActorSystem[None.type] = ActorSystem(Behaviors.empty, "test")

  val configuration = Configuration.of(
    host = "localhost",
    port = 5672,
    virtualHost = "/",
    username = "guest",
    password = "admin",
  )

  configuration.toProvider.foreach: provider =>
    val graph = QueueConsumer.start(provider, List(Queue("test1"), Queue("test2")))
    graph.run()
