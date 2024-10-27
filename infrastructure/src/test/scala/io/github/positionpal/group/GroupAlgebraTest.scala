package io.github.positionpal.group

import scala.concurrent.ExecutionContext

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorSystem
import akka.pattern.StatusReply
import cats.data.ReaderT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.mtl.Ask
import io.github.positionpal.client.ClientID
import io.github.positionpal.command.ClientJoinsGroup
import io.github.positionpal.group.GroupAlgebra.LiveGroupAlgebra
import io.github.positionpal.reply.{ClientSuccessfullyJoined, Reply}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupAlgebraTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers:

  opaque type TestF[A] = ReaderT[IO, ActorSystem[?], A]
  given ExecutionContext = system.executionContext

  private val algebra = LiveGroupAlgebra[TestF]

  "GroupAlgebra" should:
    "initialize sharding successfully" in:
      val result = algebra.initSharding.run(system).unsafeToFuture()
      result.map: _ =>
        succeed

    "get group reference and handle commands" in:
      val clientId = ClientID("test-client")
      val groupId = "test-group"

      val program = for
        _ <- algebra.initSharding
        ref <- algebra.getGroupRef(groupId)
        result <- ReaderT.liftF(
          IO.fromFuture(
            IO.delay(
              ref.ask[StatusReply[Reply]](replyTo => ClientJoinsGroup(clientId, replyTo)),
            ),
          ),
        )
      yield result

      program.run(system).unsafeToFuture().map:
        case StatusReply.Success(ClientSuccessfullyJoined(users)) => users should contain(clientId)
        case other => fail(s"Unexpected response: $other")
