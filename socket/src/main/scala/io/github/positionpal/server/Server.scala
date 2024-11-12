package io.github.positionpal.server

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import io.github.positionpal.server.routes.Routes.*

object Server:

  given actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(Behaviors.empty[Any], "ClusterSystem")
  given executionContext: ExecutionContextExecutor = actorSystem.executionContext

  def startup(): Unit =
    val binding = Http().newServerAt("localhost", 8080).bind(v1Routes)
    println("Server running...")
    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
    println("Server is shut down")

@main
def main(): Unit =
  import Server.startup
  startup()
