package io.github.positionpal.grpc

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.grpc.scaladsl.{ServerReflection, ServiceHandler as GrpcServiceHandler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import io.github.positionpal.proto.{ChatService, ChatServiceHandler}

class GrpcServer(using system: ActorSystem[?]):

  given executionContext: ExecutionContext = system.executionContext

  private val service: HttpRequest => Future[HttpResponse] =
    GrpcServiceHandler.concatOrNotFound(
      ChatServiceHandler.partial(new ServiceHandler()),
      ServerReflection.partial(List(ChatService)),
    )

  def run(): Future[ServerBinding] =
    val bound = Http().newServerAt(interface = "127.0.0.1", port = 5052).bind(service)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    import scala.util.{Failure, Success}
    bound.onComplete:
      case Success(binding) =>
        val address = binding.localAddress
        println(s"gRPC server bound to ${address.getHostString}:${address.getPort}")
      case Failure(ex) =>
        println("Failed to bind gRPC endpoint, terminating system")
        ex.printStackTrace()
        system.terminate()

    bound
