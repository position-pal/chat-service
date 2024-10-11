package io.github.positionpal.server

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import io.github.positionpal.server.routes.Routes.*

object Server:

  given actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(Behaviors.empty[Any], "messaging-actor-system")
  given executionContext: ExecutionContextExecutor = actorSystem.executionContext

  def startup(): Unit =
    val binding = Http().newServerAt("localhost", 8080).bind(defaultRoute ~ webSocketFlowRoute)
    println("Server running...")

    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
    println("Server is shut down")

@main
def main(): Unit =
  import Server.startup
  startup()
