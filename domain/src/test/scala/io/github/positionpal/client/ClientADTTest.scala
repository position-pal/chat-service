package io.github.positionpal.client

import scala.compiletime.uninitialized

import io.github.positionpal.client.ClientADT.Client
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ClientADTTest extends AnyWordSpec with Matchers with BeforeAndAfterEach:

  case class TestClient[I, O](
      id: I,
      outputRef: ClientADT.OutputReference[O],
      status: ClientADT.ClientStatus,
  ) extends ClientADT.Client[I, O]:
    def setOutputRef(reference: ClientADT.OutputReference[O]): Client[I, O] =
      copy(outputRef = reference)

    def setStatus(newStatus: ClientADT.ClientStatus): Client[I, O] =
      copy(status = newStatus)

  type BasicClient = TestClient[String, String]
  var client: BasicClient = uninitialized

  override protected def beforeEach(): Unit =
    super.beforeEach()
    client = TestClient(
      id = "test-client-1",
      outputRef = ClientADT.OutputReference.EMPTY,
      status = ClientADT.ClientStatus.OFFLINE,
    )

  "A Client" when:
    "newly created" should:
      "have the correct initial values" in:
        client.id shouldBe "test-client-1"
        client.outputRef shouldBe ClientADT.OutputReference.EMPTY
        client.status shouldBe ClientADT.ClientStatus.OFFLINE

    "updating output reference" should:
      "create new instance with updated reference" in:
        val newRef = ClientADT.OutputReference.OUT("test-channel")
        val updatedClient = client.setOutputRef(newRef)

        updatedClient.id shouldBe client.id
        updatedClient.outputRef shouldBe newRef
        updatedClient.status shouldBe client.status
        client.outputRef shouldBe ClientADT.OutputReference.EMPTY

    "updating status" should:
      "create new instance with updated status" in:
        val updatedClient = client.setStatus(ClientADT.ClientStatus.ONLINE)

        updatedClient.id shouldBe client.id
        updatedClient.outputRef shouldBe client.outputRef
        updatedClient.status shouldBe ClientADT.ClientStatus.ONLINE
        client.status shouldBe ClientADT.ClientStatus.OFFLINE

    "executing on output" should:
      "execute function when output reference is OUT" in:
        var executed = false
        val clientWithOutput = client.setOutputRef(ClientADT.OutputReference.OUT("test-channel"))

        clientWithOutput.executeOnOutput(_ => executed = true)
        executed shouldBe true

      "not execute function when output reference is EMPTY" in:
        var executed = false
        client.executeOnOutput(_ => executed = true)
        executed shouldBe false

      "correctly pass the output value" in:
        var receivedValue: String = ""
        val expectedValue = "test-channel"
        val clientWithOutput = client.setOutputRef(ClientADT.OutputReference.OUT(expectedValue))

        clientWithOutput.executeOnOutput(value => receivedValue = value)
        receivedValue shouldBe expectedValue

    "performing multiple operations" should:
      "maintain immutability across all operations" in:
        val originalClient = client
        val newRef = ClientADT.OutputReference.OUT("new-channel")

        val modified1 = originalClient.setOutputRef(newRef)
        val modified2 = modified1.setStatus(ClientADT.ClientStatus.ONLINE)

        originalClient.outputRef shouldBe ClientADT.OutputReference.EMPTY
        originalClient.status shouldBe ClientADT.ClientStatus.OFFLINE

        modified1.outputRef shouldBe newRef
        modified1.status shouldBe ClientADT.ClientStatus.OFFLINE

        modified2.outputRef shouldBe newRef
        modified2.status shouldBe ClientADT.ClientStatus.ONLINE

  "OutputReference" when:
    "comparing instances" should:
      "handle equality correctly" in:
        val ref1 = ClientADT.OutputReference.OUT("channel")
        val ref2 = ClientADT.OutputReference.OUT("channel")
        val ref3 = ClientADT.OutputReference.OUT("different")

        ref1 shouldBe ref2
        ref1 should not be ref3
        ClientADT.OutputReference.EMPTY shouldBe ClientADT.OutputReference.EMPTY
        ref1 should not be ClientADT.OutputReference.EMPTY
