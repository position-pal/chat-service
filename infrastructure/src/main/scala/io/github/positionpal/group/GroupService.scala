package io.github.positionpal.group

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.control.NoStackTrace

import akka.pattern.StatusReply
import akka.util.Timeout
import cats.MonadError
import cats.syntax.all.*
import io.github.positionpal.client.ClientID
import io.github.positionpal.command.{ClientJoinsGroup, ClientLeavesGroup}
import io.github.positionpal.group.GroupAlgebra.GroupAlgebra
import io.github.positionpal.reply.Reply
import io.github.positionpal.services.GroupHandlerService

enum GroupServiceError extends NoStackTrace:
  case GroupOperationFailed(message: String)

class GroupService[F[_]](algebra: GroupAlgebra[F])(using errorHandler: MonadError[F, Throwable], ctx: ExecutionContext)
    extends GroupHandlerService[String, ClientID, F]:
  given timeout: Timeout = 5.seconds

  import GroupServiceError.*

  override def joinGroup(groupId: String, clientId: ClientID): F[Unit] =
    for
      ref <- algebra.getGroupRef(groupId)
      _ <- errorHandler.catchNonFatal:
        ref.ask[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientId, replyTo)).map:
          case StatusReply.Success(_) => ()
          case StatusReply.Error(error) =>
            throw GroupOperationFailed(error.getMessage)
    yield ()

  override def leaveGroup(groupId: String, clientId: ClientID): F[Unit] =
    for
      ref <- algebra.getGroupRef(groupId)
      _ <- errorHandler.catchNonFatal:
        ref.ask[StatusReply[Reply]](replyTo => ClientLeavesGroup(clientId, replyTo)).map:
          case StatusReply.Success(_) => ()
          case StatusReply.Error(error) =>
            throw GroupOperationFailed(error.getMessage)
    yield ()
