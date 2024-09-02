package io.github.positionpal

import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}

object Startup:

  private val logger: Logger = LoggerFactory.getLogger(Startup.getClass)

  @main
  def main(args: String*): Unit =
    logger.info("Initializing system")
    initSystem(25525)

  /** Initialize a system on the cluster.
    *
    * @param port the port where actor should be spawned
    */
  private def initSystem(port: Int): ActorSystem[Nothing] =
    val config = ConfigFactory.parseString(s"""
         |akka.remote.artery.canonical.port=$port
         """.stripMargin).withFallback(ConfigFactory.load())
    ActorSystem[Nothing](Behaviors.empty[Nothing], "chatsystem", config)
