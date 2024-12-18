package io.github.positionpal.service

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef}
import akka.pattern.StatusReply
import akka.util.Timeout
import io.github.positionpal.client.ClientID
import io.github.positionpal.group.{
  ClientConnects,
  ClientDisconnects,
  ClientJoinsGroup,
  ClientLeavesGroup,
  ClientSuccessfullyConnected,
  ClientSuccessfullyDisconnected,
  ClientSuccessfullyJoined,
  ClientSuccessfullyLeaved,
  DeleteGroup,
  GroupCommand,
  GroupEventSourceHandler,
  SendMessage,
}
import io.github.positionpal.message.ChatMessageADT
import io.github.positionpal.message.ChatMessageADT.MessageOps
import io.github.positionpal.services.GroupHandlerService

class GroupService(actorSystem: ActorSystem[?]) extends GroupHandlerService:

  private val sharding = ClusterSharding(actorSystem)
  private given timeout: Timeout = 10.seconds
  private given ec: ExecutionContext = actorSystem.executionContext

  sharding.init:
    Entity(GroupEventSourceHandler.entityKey): entityContext =>
      GroupEventSourceHandler(entityContext.entityId)

  /** Get or create entity reference for a specific group
    *
    * @param groupId The ID of the group to get/create
    * @return Actor reference for the group entity
    */
  private def entityRefFor(groupId: String): EntityRef[GroupCommand] =
    sharding.entityRefFor(GroupEventSourceHandler.entityKey, groupId)

  override def delete(groupID: String): Future[Unit] =
    Future:
      entityRefFor(groupID) ! DeleteGroup()

  override def join(groupID: String)(clientID: ClientID): Future[List[ClientID]] =
    entityRefFor(groupID).ask(ref => ClientJoinsGroup(clientID, ref)).map:
      case StatusReply.Success(ClientSuccessfullyJoined(clients)) => clients
      case StatusReply.Error(ex) => throw ex

  override def leave(groupID: String)(clientID: ClientID): Future[ClientID] =
    entityRefFor(groupID).ask(ref => ClientLeavesGroup(clientID, ref)).map:
      case StatusReply.Success(ClientSuccessfullyLeaved(id)) => id
      case StatusReply.Error(ex) => throw ex

  override def connect(groupID: String)(clientID: ClientID, channel: ActorRef[String]): Future[ClientID] =
    entityRefFor(groupID).ask(ref => ClientConnects(clientID, channel, ref)).map:
      case StatusReply.Success(ClientSuccessfullyConnected(id)) => id
      case StatusReply.Error(ex) => throw ex

  override def disconnect(groupID: String)(clientID: ClientID): Future[ClientID] =
    entityRefFor(groupID).ask(ref => ClientDisconnects(clientID, ref)).map:
      case StatusReply.Success(ClientSuccessfullyDisconnected(id)) => id
      case StatusReply.Error(ex) => throw ex

  override def message(groupID: String)(message: MessageOps[ClientID, String]): Future[Unit] =
    entityRefFor(groupID).ask(ref => SendMessage(message, ref)).map:
      case StatusReply.Ack => ()
      case StatusReply.Error(ex) => throw ex
