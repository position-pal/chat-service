package io.github.positionpal.group

import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.pattern.StatusReply
import akka.serialization.jackson.CborSerializable

object GroupEventSourceHandler:

  sealed trait Command extends CborSerializable
  case class ClientJoinsGroup(clientID: String, replyTo: ActorRef[StatusReply[Reply]])

  sealed trait Event extends CborSerializable
  case class ClientJoinedToGroup(clientID: String)

  def entityKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Group")
