package io.github.positionpal.consumer

import scala.concurrent.{ExecutionContext, Future}

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.alpakka.amqp.javadsl.AmqpSource
import akka.stream.alpakka.amqp.{AmqpConnectionProvider, NamedQueueSourceSettings, QueueDeclaration, ReadResult}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, Graph}

object QueueConsumer:

  case class Queue(name: String)

  def graph(
      provider: AmqpConnectionProvider,
      queues: List[Queue],
  )(using ExecutionContext): Graph[ClosedShape, NotUsed] =
    GraphDSL.create():
      implicit graph =>
        import GraphDSL.Implicits.*

        val sources = queues.map: queue =>
          val settings = NamedQueueSourceSettings(provider, queue.name).withDeclaration(QueueDeclaration(queue.name))
            .withAckRequired(false)
          Source.fromGraph(AmqpSource.atMostOnceSource(settings, bufferSize = 10)).map(msg => (queue.name, msg))

        val merger = graph.add(Merge[(String, ReadResult)](queues.length))
        val messageProcessorUnit = Flow[(String, ReadResult)].mapAsync(1):
          case (queueName, msg) =>
            Future:
              println(s"from $queueName I've received message $msg")

        val sink = Sink.ignore

        sources.foreach(_ ~> merger)
        merger ~> messageProcessorUnit ~> sink

        ClosedShape

  def start(
      provider: AmqpConnectionProvider,
      queues: List[Queue],
  )(using system: ActorSystem[?]): RunnableGraph[NotUsed] =
    given ExecutionContext = system.executionContext
    RunnableGraph.fromGraph(graph(provider, queues))
