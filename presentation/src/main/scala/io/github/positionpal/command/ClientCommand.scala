package io.github.positionpal.command
import akka.actor.typed.ActorRef
import akka.serialization.jackson.CborSerializable

sealed trait ClientCommand extends CborSerializable
case class ReceiveReference(ref: ActorRef[?]) extends ClientCommand
