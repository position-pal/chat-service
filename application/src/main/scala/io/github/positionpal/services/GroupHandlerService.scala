package io.github.positionpal.services

import akka.actor.typed.ActorRef
import io.github.positionpal.client.ClientID
import io.github.positionpal.message.ChatMessageADT.MessageOps

trait GroupHandlerService[F[_], T]:

  /** Delete the group
    * @param groupID The ID of the group to delete
    * @return A [[F]] representing the completion of the operation
    */
  def delete(groupID: String): F[Unit]

  /** Join a client to a specific group
    * @param groupID The ID of the group to join
    * @param clientID The ID of the client joining
    * @return A [[F]] with [[List]] of [[ClientID]] that are currently connected to the service
    */
  def join(groupID: String)(clientID: ClientID): F[List[ClientID]]

  /** Remove a client to a specific group
    * @param groupID The ID of the group to join
    * @param clientID The ID of the client that should be removed
    * @return A [[Future]] with the [[ClientID]] that left the group
    */
  def leave(groupID: String)(clientID: ClientID): F[ClientID]

  /** Connect a client to the group. Note that the client must be a member of the group, therefore, [[GroupHandlerService.join]]
    *  should be called first.
    * @param groupID The ID of the group of the client
    * @param clientID The ID of the client that should be connected
    * @param channel An [[ActorRef]] that communicates directly with the client
    * @return A [[Future]] with the [[ClientID]] that connected to the group
    */
  def connect(groupID: String)(clientID: ClientID, channel: ActorRef[T]): F[ClientID]

  /** Disconnects a client from the group. Note that the client must be a member of the group, therefore, [[GroupHandlerService.join]]
    *  should be called first.
    * @param groupID The ID of the group of the client
    * @param clientID The ID of the client that should be connected
    * @return A [[Future]] with the [[ClientID]] that have disconnected from the group
    */
  def disconnect(groupID: String)(clientID: ClientID): F[ClientID]

  /** Send a message in a group. All client in connected status will receive this.
    * @param groupID The ID of the group of the client
    * @param message The message that should be broadcast
    * @return A [[Future]] wit the [[ClientID]] that have disconnected from the group
    */
  def message(groupID: String)(message: MessageOps[ClientID, String]): F[Unit]
