package io.github.positionpal.chat.entrypoint

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory
import io.github.positionpal.connection.AmqpConfiguration
import io.github.positionpal.connection.Connection.*
import io.github.positionpal.consumer.QueueConsumer
import io.github.positionpal.consumer.QueueConsumer.Exchange.GROUP_UPDATE
import io.github.positionpal.consumer.QueueConsumer.Queue
import io.github.positionpal.grpc.GrpcServer
import io.github.positionpal.handler.Handlers
import io.github.positionpal.server.WebsocketServer
import io.github.positionpal.service.GroupService
import org.slf4j.LoggerFactory

object Launcher:
  private val configurationReader = ConfigFactory.load()
  private val logger = LoggerFactory.getLogger("mainLauncher")
  private val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "LauncherSystem")
  logger.info("Starting up Position Pal Chat service")

  def launchManagementControllers(): Unit =
    logger.debug("Bringing up AkkaManagement system")
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

  def launchRabbitMQConsumer(): Unit =
    logger.debug("Bringing up RabbitMQ consumer")
    given ActorSystem[?] = system

    val rabbitMQConfiguration = AmqpConfiguration.of(
      host = configurationReader.getString("rabbitmq.host"),
      port = configurationReader.getInt("rabbitmq.port"),
      virtualHost = configurationReader.getString("rabbitmq.virtual-host"),
      username = configurationReader.getString("rabbitmq.auth.username"),
      password = configurationReader.getString("rabbitmq.auth.password"),
    )

    rabbitMQConfiguration.toProvider.map: provider =>
      val queues = List(
        Queue(name = configurationReader.getString("rabbitmq.services.user.queue-name"), exchanges = List(GROUP_UPDATE)),
      )
      val service = GroupService(system)
      val graph = QueueConsumer.create(provider, queues, Handlers.basic(service))
      graph.run()

  def launchGrpcServer(): Unit =
    logger.debug("Bringing up GrpcService")
    GrpcServer(system).startup()

  def launchWebsocketServer(): Unit =
    logger.debug("Bringing up WebsocketService")
    WebsocketServer(system).startup()

@main def main(): Unit =
  import Launcher.*

  launchManagementControllers()
  launchRabbitMQConsumer()
  launchGrpcServer()
  launchWebsocketServer()
