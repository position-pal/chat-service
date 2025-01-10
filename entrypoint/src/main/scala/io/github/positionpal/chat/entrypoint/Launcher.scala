package io.github.positionpal.chat.entrypoint

import io.github.positionpal.grpc.GrpcServer
import io.github.positionpal.server.WebsocketServer
import org.slf4j.LoggerFactory

@main def main(): Unit =

  val logger = LoggerFactory.getLogger("mainLauncher")
  logger.info("Starting up Position Pal Chat service")

  // Starting GRPC service
  logger.debug("Bringing up GrpcService")
  GrpcServer.startup()

  // Starting Websocket service
  logger.debug("Bringing up WebsocketService")
  WebsocketServer.startup()
