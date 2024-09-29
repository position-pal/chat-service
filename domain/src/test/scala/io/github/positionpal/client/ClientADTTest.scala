package io.github.positionpal.client

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

import ClientADT.{Client, ClientID, ClientStatus}

class ClientADTTest extends AnyWordSpecLike with Matchers:

  "Client" should:

    "change status" in:
      val clientID = ClientID("123", "email@test.it")
      val client = Client(clientID, ClientStatus.ONLINE)
      val updatedClient = client.setStatus(ClientStatus.OFFLINE)
      updatedClient.status should ===(ClientStatus.OFFLINE)




