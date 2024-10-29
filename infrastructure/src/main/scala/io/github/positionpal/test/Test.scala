package io.github.positionpal.test
import scala.concurrent.duration.*

import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.util.Timeout
import io.github.positionpal.group.GroupEventSourceHandler
import io.github.positionpal.group.GroupEventSourceHandler.entityKey

object Tests:
  given system: ActorSystem[None.type] = ActorSystem(Behaviors.empty, "ClusterSystem")
  given Timeout = 5.seconds

  @main
  def entryPoint: Unit =
    val clusterSharding = ClusterSharding(system)
    clusterSharding.init:
      Entity(entityKey)(ctx => GroupEventSourceHandler(ctx.entityId))
