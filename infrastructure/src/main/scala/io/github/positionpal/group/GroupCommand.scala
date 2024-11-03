package io.github.positionpal.group

import akka.Done
import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import io.github.positionpal.borer.BorerSerialization
import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT.ChatMessageImpl

sealed trait GroupCommand extends BorerSerialization
case class ClientJoinsGroup(
    clientID: ClientID,
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

case class ClientLeavesGroup(
    clientID: ClientID,
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

case class ClientConnects(
    clientID: ClientID,
    commChannel: ActorRef[String],
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

case class ClientDisconnects(
    clientID: ClientID,
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

case class SendMessage(
    message: ChatMessageImpl[ClientID, String],
    replyTo: ActorRef[StatusReply[Done]],
) extends GroupCommand
