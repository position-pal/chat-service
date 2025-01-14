package io.github.positionpal.consumer

import scala.concurrent.{ExecutionContext, Future}

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.alpakka.amqp.scaladsl.AmqpSource
import akka.stream.alpakka.amqp.{
  AmqpConnectionProvider,
  BindingDeclaration,
  ExchangeDeclaration,
  NamedQueueSourceSettings,
  QueueDeclaration,
  ReadResult,
}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, Graph}
import cats.syntax.either.*
import io.github.positionpal.MessageType
import io.github.positionpal.handler.MessageHandler
import org.slf4j.{Logger, LoggerFactory}

object QueueConsumer:
  /** Represents a queue configuration including its name, associated exchanges, and an optional routing key.
    *
    * @param name       The name of the queue.
    * @param exchanges  A list of exchanges associated with the queue.
    * @param routingKey An optional routing key for message routing.
    */
  case class Queue(name: String, exchanges: List[Exchange] = Nil, routingKey: Option[String] = None)

  /** Represents an exchange declaration for RabbitMQ.
    *
    * @param name         The name of the exchange.
    * @param exchangeType The type of the exchange (e.g., "direct", "topic", "headers").
    * @param routingKey   An optional routing key for binding.
    */
  enum Exchange(val name: String, val exchangeType: String, val routingKey: Option[String] = None):
    /** A predefined exchange for handling group updates. */
    case GROUP_UPDATE extends Exchange("group_updates_exchange", "headers")

  private val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  /** Creates a stream graph for consuming messages from multiple RabbitMQ queues.
    *
    * @param provider         The AMQP connection provider used to connect to RabbitMQ.
    * @param queues           A list of queues to consume from, each with its associated configuration.
    * @param messageHandler   A handler for processing messages based on their type.
    * @tparam F The effect type used by the [[MessageHandler]].
    * @return A closed Akka Stream graph that consumes messages and processes them using the provided handler.
    */
  private def graph[F[_]](provider: AmqpConnectionProvider, queues: List[Queue], messageHandler: MessageHandler[F])(
      using ExecutionContext,
  ): Graph[ClosedShape, NotUsed] =
    GraphDSL.create():
      implicit graph =>
        import GraphDSL.Implicits.*
        val sources = queues.map: queue =>

          val queueDeclaration = QueueDeclaration(queue.name)
          val exchangeDeclarations = queue.exchanges.map: exchange =>
            val declaration = ExchangeDeclaration(exchange.name, exchange.exchangeType)
            val binding = BindingDeclaration(queue = queue.name, exchange = exchange.name)
              .withRoutingKey(queue.routingKey.getOrElse(""))
            (declaration, binding)

          val settings = NamedQueueSourceSettings(provider, queue.name).withDeclarations(
            queueDeclaration +: exchangeDeclarations.flatMap(decls => List(decls._1, decls._2)),
          ).withAckRequired(false)
          Source.fromGraph(AmqpSource.atMostOnceSource(settings, bufferSize = 10)).map(msg => (queue.name, msg))

        val merger = graph.add(Merge[(String, ReadResult)](queues.length))
        val messageProcessorUnit = Flow[(String, ReadResult)].mapAsync(1):
          case (_, msg) =>
            Future:
              val header = msg.properties.getHeaders.get("message_type").toString
              val result = Either.catchOnly[IllegalArgumentException](MessageType.valueOf(header))
                .leftMap(_ => s"Error while retrieving $header")
              result match
                case Right(msgType: MessageType) => messageHandler.handle(msgType, msg.bytes)
                case e => logger.error(s"I received a message that I can't handle because: $e")

        val sink = Sink.ignore

        sources.foreach(_ ~> merger)
        merger ~> messageProcessorUnit ~> sink

        ClosedShape

  /** Starts the queue consumer by running the Akka Stream graph.
    *
    * @param provider       The AMQP connection provider used to connect to RabbitMQ.
    * @param queues         A list of queues to consume from, each with its associated configuration.
    * @param messageHandler A handler for processing messages based on their type.
    * @param system         The Akka actor system used for running the stream.
    * @tparam F The effect type used by the [[MessageHandler]].
    * @return A [[RunnableGraph]] that can be executed to start the consumer.
    */
  def create[F[_]](
      provider: AmqpConnectionProvider,
      queues: List[Queue],
      messageHandler: MessageHandler[F],
  )(using system: ActorSystem[?]): RunnableGraph[NotUsed] =
    given ExecutionContext = system.executionContext
    RunnableGraph.fromGraph(graph(provider, queues, messageHandler))
