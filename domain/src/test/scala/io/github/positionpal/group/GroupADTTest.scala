package io.github.positionpal.group

import io.github.positionpal.client.ClientADT.ClientStatus.*
import io.github.positionpal.client.ClientADT.{Client, ClientID}
import io.github.positionpal.group.GroupADT.Group
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GroupADTTest extends AnyWordSpecLike with Matchers:
  "Group" should:
    "add a new client" in:
      val group = Group.empty("test")
      val clientID = ClientID("123", "prova.email@abc.it")
      val client = Client(clientID, ONLINE)
      val newGroup = group.addClient(client)
      assert(newGroup.clients.contains(client))
