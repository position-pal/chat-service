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
  case class Queue(name: String, exchanges: List[Exchange] = Nil, routingKey: Option[String] = None)

  enum Exchange(val name: String, val exchangeType: String, val routingKey: Option[String] = None):
    case GROUP_UPDATE extends Exchange("group_updates_exchange", "headers")

  private val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  def graph(provider: AmqpConnectionProvider, queues: List[Queue], messageHandler: MessageHandler)(using
      ExecutionContext,
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

  def start(
      provider: AmqpConnectionProvider,
      queues: List[Queue],
      messageHandler: MessageHandler,
  )(using system: ActorSystem[?]): RunnableGraph[NotUsed] =
    given ExecutionContext = system.executionContext
    RunnableGraph.fromGraph(graph(provider, queues, messageHandler))
