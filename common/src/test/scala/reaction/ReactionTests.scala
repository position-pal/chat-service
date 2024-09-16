package reaction

import scala.language.postfixOps

import cats.{Id, Monad}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ReactionTests extends AnyWordSpecLike with Matchers:

  object TestOptionReaction extends OptionReaction[String]:
    override type Event = String
    override type State = Int

  import TestOptionReaction.*

  object Helpers:

    def createReaction(eventResult: Option[String]): Reaction[Id] = on: _ =>
      Monad[Id].pure(eventResult)

    def eventLengthGreaterThan(minLength: Int): Context => Boolean = { context =>
      context.event.length > minLength
    }

    def stateGreaterThan(minState: Int): Context => Boolean = { context =>
      context.state > minState
    }

  import Helpers.*

  "OptionReaction" should:

    "concatenate two successful reactions returning some results" in:

      val firstReaction: Reaction[Id] = createReaction(Some("First Success"))
      val secondReaction: Reaction[Id] = createReaction(Some("Second Success"))

      val chainedReaction: Reaction[Id] = firstReaction.andThen(secondReaction)

      val context = Context(event = "TestEvent", state = 10)
      val result: Option[String] = chainedReaction(context)

      assert(result.contains("Second Success"))

    "concatenate failing reactions returning empty results" in:

      val firstReaction: Reaction[Id] = createReaction(None)
      val secondReaction: Reaction[Id] = createReaction(Some("Second Success"))

      val chainedReaction: Reaction[Id] = firstReaction.andThen(secondReaction)

      val context = Context(event = "TestEvent", state = 10)
      val result: Option[String] = chainedReaction(context)

      assert(result.isEmpty)

    "modify state based on event and return result" in:

      val complexReaction: Reaction[Id] = on { context =>
        val newState = context.state + context.event.length
        if (newState > 15) Monad[Id].pure(Some(s"New state is: $newState"))
        else Monad[Id].pure(None)
      }

      val context1 = Context(event = "Test", state = 10)
      val result1: Option[String] = complexReaction(context1)
      assert(result1.isEmpty)

      val context2 = Context(event = "LongerEvent", state = 10)
      val result2: Option[String] = complexReaction(context2)
      assert(result2.contains("New state is: 21"))

    "should not execute second reaction when first fails filter" in:
      val firstReaction: Reaction[Id] = createReaction(Some("First Success"))
      val secondReaction: Reaction[Id] = createReaction(Some("Second Success"))

      val filteredFirstReaction = firstReaction.filter(_.state > 5)
      val chainedReaction = filteredFirstReaction.andThen(secondReaction)
      val context = Context(event = "TestEvent", state = 3)

      val result: Option[String] = chainedReaction(context)
      assert(result.isEmpty)

    "work with complex predicates based on both event and state" in:
      val reaction: Reaction[Id] = createReaction(Some("Complex Success"))

      val complexFilter = reaction.filter: context =>
        context.event.contains("Allow") && context.state > 5

      val context1 = Context(event = "AllowEvent", state = 10)
      val result1: Option[String] = complexFilter(context1)
      assert(result1.contains("Complex Success"))

      val context2 = Context(event = "BlockEvent", state = 10)
      val result2: Option[String] = complexFilter(context2)
      assert(result2.isEmpty)

      val context3 = Context(event = "AllowEvent", state = 2)
      val result3: Option[String] = complexFilter(context3)
      assert(result3.isEmpty)

    "for-comprehension with filtered reactions should execute correctly" in:

      val reaction1: Reaction[Id] = createReaction(Some("Reaction 1")).filter(eventLengthGreaterThan(5))
      val reaction2: Reaction[Id] = createReaction(Some("Reaction 2")).filter(stateGreaterThan(10))

      val composedReaction: Reaction[Id] = on { context =>
        for {
          result1 <- reaction1(context)
          result2 <- reaction2(context)
        } yield result1 + " " + result2
      }

      val context1 = Context(event = "LongEvent", state = 15)
      val result1: Option[String] = composedReaction(context1)

      assert(result1.contains("Reaction 1 Reaction 2"))

      val context2 = Context(event = "Short", state = 15)
      val result2: Option[String] = composedReaction(context2)

      assert(result2.isEmpty)

      val context3 = Context(event = "LongEvent", state = 5)
      val result3: Option[String] = composedReaction(context3)

      assert(result3.isEmpty)

      val context4 = Context(event = "Short", state = 5)
      val result4: Option[String] = composedReaction(context4)

      assert(result4.isEmpty)

    "not execute second reaction if the first one returns Some result" in:

      val firstReaction: Reaction[Id] = createReaction(Some("First Success"))
      val secondReaction: Reaction[Id] = createReaction(Some("Second Success"))

      val reactionWithOrElse: Reaction[Id] = firstReaction.orElse(secondReaction) {
        _.event.length > 5
      }

      val context = Context(event = "LongEvent", state = 10)
      val result: Option[String] = reactionWithOrElse(context)

      assert(result.contains("First Success"))

    "execute second reaction if a condition isn't met" in:

      val firstReaction: Reaction[Id] = createReaction(None)
      val secondReaction: Reaction[Id] = createReaction(Some("Second Success"))

      val reactionWithOrElse: Reaction[Id] = firstReaction.orElse(secondReaction) {
        _.event.length > 5
      }

      val context = Context(event = "Event", state = 10)
      val result: Option[String] = reactionWithOrElse(context)

      assert(result.contains("Second Success"))
