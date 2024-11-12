package io.github.positionpal.borer

import akka.actor.ExtendedActorSystem
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import io.bullet.borer.{Cbor, Decoder, Encoder}
import io.github.positionpal.client.ClientADT.OutputReference
import org.scalatest.wordspec.AnyWordSpecLike

class DefaultAkkaBorerSerializerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike:
  object TestActor:
    def apply(): Behavior[String] = Behaviors.receiveMessage(_ => Behaviors.same)

  val serializer = new DefaultAkkaBorerSerializer(system.classicSystem.asInstanceOf[ExtendedActorSystem])
  import serializer.given

  "DefaultAkkaBorerSerializer" should:
    "correctly serialize and deserialize an ActorRef" in:
      val probe = TestProbe[String]()
      val testActorRef = spawn(TestActor())

      val encoded = Cbor.encode(testActorRef).toByteArray
      val decodedRef = Cbor.decode(encoded).to[ActorRef[String]].value

      decodedRef ! "test message"
      probe.expectNoMessage()

    "correctly serialize and deserialize and ActorRef inside an OutputReference object" in:
      val testProbe = TestProbe[String]()
      val testActorRef = spawn(TestActor())

      val outputRef = OutputReference.OUT(testActorRef)
      val encoded = Cbor.encode(outputRef).toByteArray
      val decodedRef = Cbor.decode(encoded).to[OutputReference[ActorRef[String]]].value

      decodedRef match
        case OutputReference.OUT(ref) =>
          ref ! "test message"
          testProbe.expectNoMessage()
        case _ => fail("Deserialized OutputReference did not match OutputReference.OUT")

    "correctly serialize and deserialize OutputReference.EMPTY" in:
      val outputRef: OutputReference[String] = OutputReference.EMPTY
      val encoded = Cbor.encode(outputRef).toByteArray
      val decodedRef = Cbor.decode(encoded).to[OutputReference[String]].value

      decodedRef match
        case OutputReference.EMPTY => succeed
        case _ => fail("Deserialized OutputReference did not match OutputReference.EMPTY")
