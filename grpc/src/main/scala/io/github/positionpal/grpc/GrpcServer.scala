package io.github.positionpal.grpc

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.scaladsl.{ServerReflection, ServiceHandler as GrpcServiceHandler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.typesafe.config.ConfigFactory
import io.github.positionpal.proto.{ChatService, ChatServiceHandler}
import org.slf4j.LoggerFactory

object GrpcServer:

  private val logger = LoggerFactory.getLogger(getClass.getName)

  given system: ActorSystem[Any] = ActorSystem(Behaviors.empty[Any], "GrpcSystem")
  given executionContext: ExecutionContext = system.executionContext

  private val service: HttpRequest => Future[HttpResponse] =
    GrpcServiceHandler.concatOrNotFound(
      ChatServiceHandler.partial(new ServiceHandler()),
      ServerReflection.partial(List(ChatService)),
    )

  def startup(): Future[ServerBinding] =
    val config = ConfigFactory.load()
    val port = config.getInt("grpc.service.default-grpc-port")
    val interface = config.getString("grpc.service.grpc-bind-interface")

    val bound = Http().newServerAt(interface = interface, port = port).bind(service)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    import scala.util.{Failure, Success}
    bound.onComplete:
      case Success(binding) =>
        val address = binding.localAddress
        logger.info(s"gRPC server bound to ${address.getHostString}:${address.getPort}")
      case Failure(ex) =>
        logger.info("Failed to bind gRPC endpoint, terminating system")
        ex.printStackTrace()
        system.terminate()

    bound
