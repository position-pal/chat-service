package io.github.positionpal.group

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.pattern.StatusReply.Success
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import io.github.positionpal.group.Group.State
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

class GroupSpec
    extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
    with AnyWordSpecLike
    with BeforeAndAfterEach:

  private val eventSourcedBehaviorTestKit = EventSourcedBehaviorTestKit[Command, Event, State](system, Group("test"))

  override protected def beforeEach(): Unit =
    super.beforeEach()
    eventSourcedBehaviorTestKit.clear()

  "Group" should:

    "add new user" in:
      val result1 = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](actorRef => UserEnterToGroup("usr1", actorRef))
      result1.reply.isSuccess should ===(true)
      result1.reply should ===(Success(Reply(List("usr1"))))

    "remove user" in:
      val result1 = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](actorRef => UserEnterToGroup("usr1", actorRef))
      result1.reply.isSuccess should ===(true)
      val result2 = eventSourcedBehaviorTestKit
        .runCommand[StatusReply[Reply]](actorRef => UserLeaveFromGroup("usr1", actorRef))
      result2.reply.isSuccess should ===(true)
      result2.reply should ===(Success(Reply(List())))
