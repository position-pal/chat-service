package io.github.positionpal.test

import scala.concurrent.ExecutionContext

import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import io.github.positionpal.connection.Configuration
import io.github.positionpal.connection.Connection.*
import io.github.positionpal.consumer.QueueConsumer
import io.github.positionpal.consumer.QueueConsumer.Exchange.GROUP_UPDATE
import io.github.positionpal.consumer.QueueConsumer.Queue
import io.github.positionpal.handler.Handlers
import io.github.positionpal.utils.AmqpWriter
import io.github.positionpal.{AddedMemberToGroup, AvroSerializer, MessageType, User}

def startActorSystem(name: String, hostname: String, port: Int): ActorSystem[None.type] =
  ActorSystem(
    Behaviors.empty,
    name,
    ConfigFactory.parseString(s"""
      akka {
        actor {
          provider = cluster
        }
        remote.artery {
          canonical {
            hostname = "$hostname"
            port = "$port"
          }
        }
        cluster {
          downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"
          seed-nodes = [
            "akka://test@127.0.0.1:2551",
          ]
        }
      }
    """),
  )

@main
def startSeedNode(): Unit = ActorSystem(Behaviors.empty, "test")

@main
def graphTest(): Unit =
  given system: ActorSystem[None.type] = startActorSystem("consumer", "127.0.0.1", 2552)

  val configuration = Configuration.of(
    host = "localhost",
    port = 5672,
    virtualHost = "/",
    username = "guest",
    password = "admin",
  )

  configuration.toProvider.foreach: provider =>
    val queues = List(
      Queue(name = "test1", exchanges = List(GROUP_UPDATE)),
    )

    val graph = QueueConsumer.start(provider, queues, Handlers.basic)
    graph.run()

@main
def utilsTest(): Unit =
  given system: ActorSystem[None.type] = startActorSystem("producer", "127.0.0.1", 2553)
  given ExecutionContext = system.executionContext

  val serializer = AvroSerializer()
  val user = User.create("uid-test", "name-test", "surname-test", "email-test", "role-test")
  val event = AddedMemberToGroup.create("123", user)

  val message = ByteString(serializer.serializeAddedMemberToGroup(event))
  val messageType = MessageType.MEMBER_ADDED
  AmqpWriter.send(message, messageType, GROUP_UPDATE.name, "test1").onComplete:
    case scala.util.Success(writeResult) =>
      println(s"Message sent successfully: $writeResult")
    case scala.util.Failure(exception) =>
      println(s"Failed to send message: ${exception.getMessage}")
