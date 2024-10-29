package io.github.positionpal.event

import akka.actor.typed.ActorRef
import akka.serialization.jackson.CborSerializable

sealed trait ClientEvent extends CborSerializable
case class NewReference(ref: ActorRef[?]) extends ClientEvent
