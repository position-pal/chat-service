package io.github.positionpal.group

import io.github.positionpal.group.GroupADT.{ErrorCode, Group}
import io.github.positionpal.group.GroupDSL.*
import io.github.positionpal.utils.{ExternalRefOps, StringContainer}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupDSLTest extends AnyWordSpecLike with Matchers:

  val refClient1 = "id1"
  val refClient2 = "id2"
  val client1: StringContainer = StringContainer("test1")
  val client2: StringContainer = StringContainer("test2")
  val emptyGroup: Group[String, ExternalRefOps] = Group.empty[String, ExternalRefOps]("testEmptyGroup")
  val clientGroup: Group[String, ExternalRefOps] = Group(Map(refClient1 -> client1), "testGroupWithClients")

  "GroupDSL" should:
    "add a client using + operator" in:
      val client1 = StringContainer("test1")
      val result = emptyGroup + (refClient1 -> client1)

      result.isRight shouldBe true
      result.map(_.isPresent(refClient1)) shouldBe Right(true)
      result.flatMap(g => g ? refClient1).map(_.containedString) shouldBe Right("test1")

    "remove a client using - operator" in:
      val result = clientGroup - refClient1

      result.isRight shouldBe true
      result.map(_.isPresent(refClient1)) shouldBe Right(false)
      result.map(_.clientIDList) shouldBe Right(List())

    "get a client using ? operator" in:
      val result = clientGroup ? refClient1

      result.isRight shouldBe true
      result.map(_.containedString) shouldBe Right("test1")

    "fail to get a non-existent client with ?" in:
      val result = emptyGroup ? refClient1
      result shouldBe Left(ErrorCode.ClientDoesntExists(refClient1))

    "update a client using ~> operator" in:
      val result = clientGroup.~>(refClient1):
        _.executeCommand(_.toUpperCase)

      result.isRight shouldBe true
      result.flatMap(g => g ? refClient1).map(_.containedString) shouldBe Right("TEST1")

    "execute command using |> operator" in:
      val result = clientGroup.|>(refClient1): ref =>
        ref.executeCommand(_.toUpperCase).containedString

      result shouldBe Right("TEST1")

    "execute conditional command using ?> operator with true condition" in:
      val result = clientGroup.?>(refClient1)(_.containedString.length > 3) {
        _.executeCommand(_.toUpperCase).containedString
      } {
        _.executeCommand(_.reverse).containedString
      }

      result shouldBe Right("TEST1")

    "execute conditional command using ?> operator with false condition" in:
      val result = clientGroup.?>(refClient1)(_.containedString.length < 3) {
        _.executeCommand(_.toUpperCase).containedString
      } {
        _.executeCommand(_.reverse).containedString
      }

      result shouldBe Right("1tset")

    "execute command on all clients using |>> operator" in:
      var results = List.empty[String]
      val group =
        for grp <- clientGroup + (refClient2 -> client2)
        yield grp

      group match
        case Right(updated) =>
          updated.|>> { ref =>
            results = results :+ ref.executeCommand(_.toUpperCase).containedString
          }
          results should contain allOf ("TEST1", "TEST2")
        case _ => fail("can't update group with new client")

    "support chaining DSL operations" in:
      val result = for
        g1 <- emptyGroup + (refClient1 -> client1)
        g2 <- g1 + (refClient2 -> client2)
        g3 <- g2.~>(refClient1): ref =>
          ref.executeCommand(_.toUpperCase)
        g4 <- g3.~>(refClient2): ref =>
          ref.executeCommand(_.reverse)
      yield g4

      result.isRight shouldBe true
      result.flatMap(g => g ? refClient1).map(_.containedString) shouldBe Right("TEST1")
      result.flatMap(g => g ? refClient2).map(_.containedString) shouldBe Right("2tset")

    "compose multiple operations in a single expression" in:
      val result = for
        g1 <- emptyGroup + (refClient1 -> client1)
        g2 <- g1.~>(refClient1)(_.executeCommand(_.toUpperCase))
        uppercaseStr <- g2.|>(refClient1)(_.containedString)
        g3 <- g2.~>(refClient1)(_.executeCommand(s => s + s))
        finalStr <- g3.|>(refClient1)(_.containedString)
      yield (uppercaseStr, finalStr)
      result shouldBe Right(("TEST1", "TEST1TEST1"))
