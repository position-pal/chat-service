package io.github.positionpal.command

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.serialization.jackson.CborSerializable
import io.github.positionpal.client.ClientID
import io.github.positionpal.reply.Reply

sealed trait Command extends CborSerializable
case class ClientJoinsGroup(clientID: ClientID, replyTo: ActorRef[StatusReply[Reply]]) extends Command
case class ClientLeavesGroup(clientID: ClientID, replyTo: ActorRef[StatusReply[Reply]]) extends Command
