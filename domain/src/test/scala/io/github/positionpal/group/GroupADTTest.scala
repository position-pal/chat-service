package io.github.positionpal.group

import io.github.positionpal.group.GroupADT.ErrorCode.{ClientAlreadyPresent, ClientDoesntExists}
import io.github.positionpal.group.GroupADT.Group
import io.github.positionpal.utils.{ExternalRefOps, StringContainer}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupADTTest extends AnyWordSpecLike with Matchers:
  "Group" should:
    "add a new client" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = "123"
      val updatedGroup = group.addClient(clientID, container)
      updatedGroup match
        case Right(group @ Group(_, _)) => assert(group.clientIDList.contains(clientID))
        case _ => fail("Error while trying to insert a client in a group")

    "remove a client" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = "123"

      val updatedGroup = for
        grp1 <- group.addClient(clientID, container)
        remGrp <- grp1.removeClient(clientID)
      yield remGrp

      updatedGroup match
        case Right(group @ Group(_, _)) => assert(!group.clientIDList.contains(clientID))
        case _ => fail("Error while trying to remove a client in a group")

    "check if a client is already inside a Group" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = "1a24"

      val updatedGroup =
        for grp1 <- group.addClient(clientID, container)
        yield grp1

      updatedGroup match
        case Right(group @ Group(_, _)) => assert(group.isPresent(clientID))
        case _ => fail("Error while trying to insert a client in a group")

    "return a error code while trying to insert a client with the same ClientID" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val container2 = StringContainer("testString2")
      val clientID = "1b23"

      val updatedGroup = for
        grp1 <- group.addClient(clientID, container)
        grp2 <- grp1.addClient(clientID, container2)
      yield grp2

      updatedGroup match
        case Left(ClientAlreadyPresent(cl)) => assert(cl == clientID)
        case _ => fail("It should return a ClientAlreadyPresent error code")

    "execute an operation on all the external references of the group" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = "1b23"
      group.addClient(clientID, container) match
        case Right(grp @ Group(_, _)) =>
          grp.executeOnClients: externalRef =>
            val newRef = externalRef.executeCommand(_.toUpperCase)
            assert(newRef.containedString == "TESTSTRING")
        case _ => fail("Error while trying to insert a client in a group")

    "execute an operation on a specific client by ID" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = "1c34"

      val result = for
        grp <- group.addClient(clientID, container)
        executionResult <- grp.executeOnClient(clientID)(_.containedString.toUpperCase)
      yield executionResult

      result match
        case Right(resultString) => assert(resultString == "TESTSTRING")
        case _ => fail("Failed to execute operation on client")

    "return an error when trying to execute on a non-existent client" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val clientID = "1d45"

      group.executeOnClient(clientID)(_.containedString) match
        case Left(ClientDoesntExists(cl)) => assert(cl == clientID)
        case _ => fail("Should return ClientDoesntExists error")

    "execute ok function when condition is true" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = "1e56"

      val result = for
        grp <- group.addClient(clientID, container)
        executionResult <- grp.executeOnCondition(clientID)(
          _.containedString.startsWith("test"),
        )(
          client => client.containedString.toUpperCase,
          client => client.containedString.toLowerCase,
        )
      yield executionResult

      result match
        case Right(resultString) => assert(resultString == "TESTSTRING")
        case _ => fail("Failed to execute ok function")

    "ddfsadf an error when trying to execute on a non-existent client" in:
      val group = Group.empty[String, ExternalRefOps]("testGroup")
      val clientID = "1g78"

      group.executeOnCondition(clientID)(_ => true)(
        _ => "ok",
        _ => "ko",
      ) match
        case Left(ClientDoesntExists(cl)) => assert(cl == clientID)
        case _ => fail("Should return ClientDoesntExists error")
