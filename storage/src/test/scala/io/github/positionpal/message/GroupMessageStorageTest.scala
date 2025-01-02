package io.github.positionpal.message

import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class GroupMessageStorageTest extends AsyncWordSpecLike with Matchers with BeforeAndAfterAll:

  private val testBehavior: Behavior[Nothing] = Behaviors.empty
  given system: ActorSystem[Nothing] = ActorSystem(
    testBehavior,
    "GroupMessageStorageTest",
    ConfigFactory.parseString("""
     akka.actor {
        allow-java-serialization = on
        provider = "cluster"
        serializers {
          jackson-cbor = "akka.serialization.jackson.JacksonCborSerializer"
          borer-cbor = "io.github.positionpal.serializer.AkkaSerializer"
        }
        serialization-bindings {
          "io.github.positionpal.borer.BorerSerialization" = borer-cbor
        }
     }

     akka.remote.artery.canonical {
        hostname = "127.0.0.1"
        port = 0
     }

     akka.cluster {
        seed-nodes = ["akka://GroupMessageStorageTest@127.0.0.1:0"]
        downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"

        jmx.enabled = off
        shutdown-after-unsuccessful-join-seed-nodes = 20s
     }

     akka.persistence{
        journal.plugin = "akka.persistence.cassandra.journal"
        journal.auto-start-journals = ["akka.persistence.cassandra.journal"]

        cassandra {
          journal.keyspace = "chatservice"

          connection-timeout = 30s
          init-retry-interval = 2s
          init-retry-max = 15
        }
     }
    """).withFallback(ConfigFactory.defaultApplication()),
  )

  private val cluster = Cluster(system)
  private val storage = new GroupMessageStorage()

  override def beforeAll(): Unit =
    super.beforeAll()
    cluster.join(cluster.selfMember.address)

  "GroupMessageStorage" should:
    "retrieve last messages from a known group" in:

      val knownGroupId = "a123"

      storage.getLastMessages(knownGroupId)(3).map: messages =>
        println(messages)

        messages should not be empty
        messages.length should be <= 3

        messages.map(_.strip()) should be(Seq("It's getting late, I think", "See you then", "bye!"))

    "handle a group with no messages" in:
      val emptyGroupId = "empty-group"

      storage.getLastMessages(emptyGroupId)(5).map: messages =>
        messages should be(empty)

    "handle requesting more messages than available" in:
      val targetGroup = "a123"

      storage.getLastMessages(targetGroup)(10).map: messages =>
        messages should not be empty
        messages.length should be <= 9
