package io.github.positionpal.chat.entrypoint

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import io.github.positionpal.grpc.GrpcServer
import io.github.positionpal.server.WebsocketServer
import org.slf4j.LoggerFactory

@main def main(): Unit =

  val logger = LoggerFactory.getLogger("mainLauncher")
  logger.info("Starting up Position Pal Chat service")

  val actorSystem: ActorSystem[?] = ActorSystem(Behaviors.empty, "ManagementSystem")

  // Starting up management and bootstrap
  logger.debug("Bringing up AkkaManagement system")
  AkkaManagement(actorSystem).start()
  ClusterBootstrap(actorSystem).start()

  // Starting GRPC service
  logger.debug("Bringing up GrpcService")
  GrpcServer.startup()

  // Starting Websocket service
  logger.debug("Bringing up WebsocketService")
  WebsocketServer.startup()
