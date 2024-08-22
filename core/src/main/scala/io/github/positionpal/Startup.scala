package io.github.positionpal

import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Startup:
  
  val logger = LoggerFactory.getLogger(Startup.getClass)
  
  @main
  def main(args: String*): Unit =
    logger.info("Initializing system")
    initSystem(25525)
    
    
  def initSystem(port: Int): Unit =
    val config = ConfigFactory.parseString(s"""
         |akka.remote.artery.canonical.port=$port
         """.stripMargin)
      .withFallback(ConfigFactory.load())
    ActorSystem[Nothing](Behaviors.empty[Nothing], "chatsystem", config)
    