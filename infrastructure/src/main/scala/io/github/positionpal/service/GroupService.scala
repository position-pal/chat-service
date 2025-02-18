package io.github.positionpal.service

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef}
import akka.pattern.StatusReply
import akka.util.Timeout
import io.github.positionpal.client.ClientCommunications.CommunicationProtocol
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
import io.github.positionpal.message.ChatMessageADT.Message
import io.github.positionpal.services.GroupHandlerService
import org.slf4j.LoggerFactory

class GroupService(actorSystem: ActorSystem[?]) extends GroupHandlerService[Future, CommunicationProtocol]:

  private val logger = LoggerFactory.getLogger(getClass.getName)
  private val sharding = ClusterSharding(actorSystem)
  given timeout: Timeout = 10.seconds
  given ec: ExecutionContext = actorSystem.executionContext

  logger.info("Group Service is starting")
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
    logger.debug(s"Deleting $groupID")
    Future:
      entityRefFor(groupID) ! DeleteGroup()

  override def join(groupID: String)(clientID: ClientID): Future[List[ClientID]] =
    logger.debug(s"${clientID.value} is joining to $groupID")
    entityRefFor(groupID).ask(ref => ClientJoinsGroup(clientID, ref)).map:
      case StatusReply.Success(ClientSuccessfullyJoined(clients)) => clients
      case StatusReply.Error(ex) => throw ex

  override def leave(groupID: String)(clientID: ClientID): Future[ClientID] =
    logger.debug(s"${clientID.value} is leaving from $groupID")
    entityRefFor(groupID).ask(ref => ClientLeavesGroup(clientID, ref)).map:
      case StatusReply.Success(ClientSuccessfullyLeaved(id)) => id
      case StatusReply.Error(ex) => throw ex

  override def connect(
      groupID: String,
  )(clientID: ClientID, channel: ActorRef[CommunicationProtocol]): Future[ClientID] =
    logger.debug(s"${clientID.value} is connecting to $groupID using $channel as connection channel")
    entityRefFor(groupID).ask(ref => ClientConnects(clientID, channel, ref)).map:
      case StatusReply.Success(ClientSuccessfullyConnected(id)) => id
      case StatusReply.Error(ex) => throw ex

  override def disconnect(groupID: String)(clientID: ClientID): Future[ClientID] =
    logger.debug(s"${clientID.value} is disconnecting from $groupID")
    entityRefFor(groupID).ask(ref => ClientDisconnects(clientID, ref)).map:
      case StatusReply.Success(ClientSuccessfullyDisconnected(id)) => id
      case StatusReply.Error(ex) => throw ex

  override def message(groupID: String)(message: Message[ClientID, String]): Future[Unit] =
    logger.debug(s"Sending message with text \"${message.text}\" to $groupID")
    entityRefFor(groupID).ask(ref => SendMessage(message, ref)).map:
      case StatusReply.Ack => ()
      case StatusReply.Error(ex) => throw ex
