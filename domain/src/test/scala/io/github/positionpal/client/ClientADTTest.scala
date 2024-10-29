package io.github.positionpal.client

import scala.language.postfixOps

import io.github.positionpal.client.ClientADT.{Client, ClientStatus}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ClientADTTest extends AnyWordSpecLike with Matchers:

  "Client" should:

    "change status" in:
      val clientID = "123"
      val client = Client.empty(clientID)
      val updatedClient = client.setStatus(ClientStatus.OFFLINE)
      updatedClient.status should ===(ClientStatus.OFFLINE)
