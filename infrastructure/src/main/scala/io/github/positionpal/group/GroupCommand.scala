package io.github.positionpal.group

import akka.Done
import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import io.github.positionpal.borer.BorerSerialization
import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT.MessageOps

sealed trait GroupCommand extends BorerSerialization

/** Command for deleting the group.
  * This is a poison pill for the group event source entity.
  */
case class DeleteGroup() extends GroupCommand

/** Client that join a group.
  * @param clientID The client reference to pass to the group
  * @param replyTo Who receives the response of the command
  */
case class ClientJoinsGroup(
    clientID: ClientID,
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

/** Client that leaves a group.
  * @param clientID The client reference to pass to the group
  * @param replyTo  Who receives the response of the command
  */
case class ClientLeavesGroup(
    clientID: ClientID,
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

/** Client connecting to the group it belongs to.
  * @param clientID The client reference to pass to the group
  * @param commChannel Reference of the actual communication channel that is in charge of sending back messages to the group
  * @param replyTo Who receives the response of the command
  */
case class ClientConnects(
    clientID: ClientID,
    commChannel: ActorRef[String],
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

/** Client disconnecting to the group it belongs to.
  * @param clientID The client reference to pass to the group
  * @param replyTo  Who receives the response of the command
  */
case class ClientDisconnects(
    clientID: ClientID,
    replyTo: ActorRef[StatusReply[Reply]],
) extends GroupCommand

/** Send a message to the clients inside the group
  * @param message The message to broadcast
  * @param replyTo Who receives the response of the command
  */
case class SendMessage(
    message: MessageOps[ClientID, String],
    replyTo: ActorRef[StatusReply[Done]],
) extends GroupCommand
