package io.github.positionpal.utils

import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cats.effect.kernel.Deferred
import cats.effect.std.Dispatcher
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

object AkkaUtils:
  private case class ShutdownContext(
      awaitCancel: Deferred[IO, Unit],
      awaitTermination: Deferred[IO, Unit],
      logger: Logger,
  )

  private case class SystemConfig[T](
      systemName: String,
      behavior: Behavior[T],
      config: Config,
      useIOExecutionContext: Boolean,
      timeoutAwaitCatsEffect: Duration,
      timeoutAwaitAkkaTermination: Duration,
  )

  private def setupExecutionContext(useIOExecutionContext: Boolean): IO[Unit] =
    Option.when(useIOExecutionContext)(IO.executionContext).sequence.void

  private def createShutdownContext: IO[ShutdownContext] =
    for
      awaitCancel <- Deferred[IO, Unit]
      awaitTermination <- Deferred[IO, Unit]
      logger = LoggerFactory.getLogger(getClass)
    yield ShutdownContext(awaitCancel, awaitTermination, logger)

  private def createAndConfigureSystem[T](
      config: SystemConfig[T],
      context: ShutdownContext,
      dispatcher: Dispatcher[IO],
  ): IO[ActorSystem[T]] =
    IO:
      val system = ActorSystem[T](
        guardianBehavior = config.behavior,
        name = config.systemName.trim.replaceAll("\\W+", "-"),
        config = config.config,
      )
      configureShutdownTasks(system, context, dispatcher, config.timeoutAwaitCatsEffect)
      system

  private def configureShutdownTasks[T](
      system: ActorSystem[T],
      context: ShutdownContext,
      dispatcher: Dispatcher[IO],
      timeoutAwaitCatsEffect: Duration,
  ): Unit =
    val shutdown = CoordinatedShutdown(system)
    shutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "sync-with-cats-effect"): () =>
      dispatcher.unsafeToFuture:
        context.awaitCancel.get.timeout(timeoutAwaitCatsEffect).recoverWith:
          case _: TimeoutException =>
            IO(context.logger.error("Timed out waiting for Cats-Effect to catch up!"))
        .as(Done)

    shutdown.addTask(CoordinatedShutdown.PhaseActorSystemTerminate, "signal-actor-system-terminated"): () =>
      dispatcher.unsafeToFuture(context.awaitTermination.complete(()).as(Done))

  private def handleTermination[T](
      system: ActorSystem[T],
      context: ShutdownContext,
      timeoutAwaitAkkaTermination: Duration,
  ): IO[Unit] =
    IO.fromFuture(IO(system.whenTerminated)).void.timeoutAndForget(timeoutAwaitAkkaTermination)
      .handleErrorWith(_ => IO(context.logger.warn("Timed-out waiting for Akka to terminate!")))

  private def createCancellationEffect[T](
      system: ActorSystem[T],
      context: ShutdownContext,
      timeoutAwaitAkkaTermination: Duration,
  ): IO[Unit] =
    for
      _ <- context.awaitCancel.complete(())
      _ <- IO(context.logger.warn("Shutting down actor system!"))
      _ <- IO(system.terminate())
      _ <- context.awaitTermination.get
      _ <- handleTermination(system, context, timeoutAwaitAkkaTermination)
    yield ()

  private def createActorSystemResource[T](
      config: SystemConfig[T],
      dispatcher: Dispatcher[IO],
  ): Resource[IO, ActorSystem[T]] =
    Resource[IO, ActorSystem[T]]:
      for
        _ <- setupExecutionContext(config.useIOExecutionContext)
        context <- createShutdownContext
        system <- createAndConfigureSystem(config, context, dispatcher)
      yield (system, createCancellationEffect(system, context, config.timeoutAwaitAkkaTermination))

  def startTypedSystem[T](
      systemName: String,
      behavior: Behavior[T] = Behaviors.empty,
      config: Config,
      useIOExecutionContext: Boolean,
      timeoutAwaitCatsEffect: Duration,
      timeoutAwaitAkkaTermination: Duration,
  ): Resource[IO, ActorSystem[T]] =
    for
      dispatcher <- Dispatcher.parallel[IO](await = true)
      system <- createActorSystemResource(
        SystemConfig(
          systemName,
          behavior,
          config,
          useIOExecutionContext,
          timeoutAwaitCatsEffect,
          timeoutAwaitAkkaTermination,
        ),
        dispatcher,
      )
    yield system
