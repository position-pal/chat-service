package io.github.positionpal.server

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import io.github.positionpal.client.CommunicationProtocol
import io.github.positionpal.server.routes.v1.Routes.*
import io.github.positionpal.service.GroupService
import io.github.positionpal.services.GroupHandlerService
import org.slf4j.LoggerFactory

case class WebsocketServer(system: ActorSystem[Any]):

  private val logger = LoggerFactory.getLogger(getClass.getName)

  given ActorSystem[Any] = system
  given service: GroupHandlerService[Future, CommunicationProtocol] = GroupService(system)
  given executionContext: ExecutionContextExecutor = system.executionContext

  /** Startup the Websocket Server */
  def startup(): Unit =

    val config = ConfigFactory.load()
    val port = config.getInt("akka.http.server.default-http-port")
    val interface = config.getString("akka.http.server.bind-interface")

    val binding = Http().newServerAt(interface, port).bind(v1Routes)
    binding.onComplete:
      case Success(infos) =>
        val address = infos.localAddress
        logger.info(s"WebSocket server started on port ${address.getPort}")

      case Failure(ex) =>
        logger.error("Error on Starting up the system", ex)
        system.terminate()
