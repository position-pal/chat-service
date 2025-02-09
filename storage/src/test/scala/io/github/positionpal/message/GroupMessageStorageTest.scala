package io.github.positionpal.message

import scala.language.postfixOps

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.EitherValues.convertEitherToValuable
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class GroupMessageStorageTest extends AsyncWordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll:

  private val testBehavior: Behavior[Nothing] = Behaviors.empty
  given system: ActorSystem[Nothing] = ActorSystem(
    testBehavior,
    "GroupMessageStorageTest",
    ConfigFactory.load("local-config.conf").withFallback(ConfigFactory.defaultApplication()),
  )

  private val cluster = Cluster(system)
  private val storage = GroupMessageStorage(system)

  override def beforeAll(): Unit =
    super.beforeAll()
    cluster.join(cluster.selfMember.address)

  "GroupMessageStorage" should:
    "retrieve last messages from a known group" in:

      val knownGroupId = "a123"
      storage.getLastMessages(knownGroupId)(3).map: result =>

        result.isRight should be(true)

        val messages = result.value
        messages should not be empty
        messages.length should be <= 3

        messages.map(_.text).map(_.strip()) should be(Seq("It's getting late, I think", "See you then", "bye!"))

    "handle a group with no messages" in:
      val emptyGroupId = "empty-group"
      storage.getLastMessages(emptyGroupId)(5).map: result =>
        result.isRight should be(true)
        result.value.length should be <= 0

    "handle requesting more messages than available" in:
      val targetGroup = "a123"
      storage.getLastMessages(targetGroup)(10).map: result =>

        result.isRight should be(true)

        val messages = result.value
        messages should not be empty
        messages.length should be <= 9
