package io.github.positionpal.group

import scala.util.control.NoStackTrace

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef}
import cats.MonadError
import cats.mtl.Ask
import cats.syntax.all.*
import io.github.positionpal.command.Command

object GroupAlgebra:

  trait GroupAlgebra[F[_]]:
    def initSharding: F[ActorRef[ShardingEnvelope[Command]]]
    def getGroupRef(groupId: String): F[EntityRef[Command]]

  sealed trait GroupError extends NoStackTrace
  object GroupError:
    case class SystemNotAvailable(msg: String) extends GroupError
    case class ShardingError(msg: String) extends GroupError

  class LiveGroupAlgebra[F[_]](using ME: MonadError[F, Throwable], A: Ask[F, ActorSystem[?]]) extends GroupAlgebra[F]:

    private def getSharding: F[ClusterSharding] =
      A.ask.flatMap: system =>
        ME.catchNonFatal(ClusterSharding(system)).adaptError:
          case e =>
            GroupError.SystemNotAvailable(e.getMessage)

    override def initSharding: F[ActorRef[ShardingEnvelope[Command]]] =
      getSharding.flatMap: sharding =>
        ME.catchNonFatal:
          sharding.init:
            Entity(GroupEventSourceHandler.entityKey): ctx =>
              GroupEventSourceHandler(ctx.entityId)
        .adaptError:
          case e => GroupError.ShardingError(s"Failed to initialize sharding: ${e.getMessage}")

    override def getGroupRef(groupId: String): F[EntityRef[Command]] =
      getSharding.map: sharding =>
        sharding.entityRefFor(GroupEventSourceHandler.entityKey, groupId)

  object LiveGroupAlgebra:
    def apply[F[_]](using ME: MonadError[F, Throwable], A: Ask[F, ActorSystem[?]]): GroupAlgebra[F] =
      new LiveGroupAlgebra[F]
