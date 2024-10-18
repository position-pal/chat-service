package io.github.positionpal.group

import io.github.positionpal.client.ClientADT.ClientID
import io.github.positionpal.group.GroupADT.ErrorCode.ClientAlreadyPresent
import io.github.positionpal.group.GroupADT.Group
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupADTTest extends AnyWordSpecLike with Matchers:

  trait ExternalRefOps:
    def containedString: String
    def executeCommand(command: String => String): ExternalRefOps

  case class StringContainer(containedString: String) extends ExternalRefOps:
    override def executeCommand(command: String => String): ExternalRefOps = StringContainer(command(containedString))

  "Group" should:
    "add a new client" in:
      val group = Group.empty[ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = ClientID("1b23", "email@prova.it")
      val updatedGroup = group.addClient(clientID, container)
      updatedGroup match
        case Right(group @ Group(_, _)) => assert(group.clientIDList.contains(clientID))
        case _ => fail("Error while trying to insert a client in a group")

    "return a error code while trying to insert a client with the same ClientID" in:
      val group = Group.empty[ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val container2 = StringContainer("testString2")
      val clientID = ClientID("1b23", "email@prova.it")
      val updatedGroup = for
        grp1 <- group.addClient(clientID, container)
        grp2 <- grp1.addClient(clientID, container2)
      yield grp2

      updatedGroup match
        case Left(ClientAlreadyPresent(cl)) => assert(cl == clientID)
        case _ => fail("Error while trying to insert a client in a group")

    "execute an operation on all the external references of the group" in:
      val group = Group.empty[ExternalRefOps]("testGroup")
      val container = StringContainer("testString")
      val clientID = ClientID("1b23", "email@prova.it")
      group.addClient(clientID, container) match
        case Right(grp @ Group(_, _)) =>
          grp.executeOnClients: externalRef =>
            val newRef = externalRef.executeCommand(_.toUpperCase)
            assert(newRef.containedString == "TESTSTRING")
        case _ => fail("Error while trying to insert a client in a group")
