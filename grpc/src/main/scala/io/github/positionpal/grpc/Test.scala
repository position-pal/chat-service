package io.github.positionpal.grpc

import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors

@main
def test: Unit =

  given ActorSystem[?] = ActorSystem[Nothing](Behaviors.empty, "testing-system")

  val server = GrpcServer()
  server.run()
