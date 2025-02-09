package io.github.positionpal.message

import scala.concurrent.{ExecutionContext, Future, TimeoutException}

import akka.actor.typed.ActorSystem
import akka.pattern.AskTimeoutException
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.persistence.typed.PersistenceId
import akka.stream.ConnectionException
import akka.stream.scaladsl.Sink
import com.datastax.oss.driver.api.core.servererrors.QueryExecutionException
import io.github.positionpal.client.ClientID
import io.github.positionpal.group.{GroupEventSourceHandler, Message as GroupMessage}
import io.github.positionpal.message.ChatMessageADT.Message
import io.github.positionpal.storage.MessageStorage

case class GroupMessageStorage(system: ActorSystem[?]) extends MessageStorage[Future]:

  given ActorSystem[?] = system
  given ExecutionContext = system.executionContext
  private val readJournal: CassandraReadJournal = PersistenceQuery(system)
    .readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  override def getLastMessages(groupID: String)(n: Int): Future[Either[ErrorDescription, MessageList]] =
    val persistenceId = PersistenceId(GroupEventSourceHandler.entityKey.name, groupID)

    readJournal.currentEventsByPersistenceId(persistenceId.id, 0L, Long.MaxValue).collect: envelope =>
      envelope.event match
        case message: GroupMessage => ChatMessageADT.message(message.text, message.time, message.from, groupID)
    .runWith(Sink.seq).map(messages => Right(messages.takeRight(n))).recover:
      case e: IllegalArgumentException => Left((s"Invalid group ID: $groupID", e))

      case e: ConnectionException => Left(("Unable to connect to Cassandra", e))

      case e: QueryExecutionException => Left(("Error while executing query to the Database", e))

      case e: AskTimeoutException => Left(("Query timed out", e))

      case e => Left(("Unexpected error", e))
    .recoverWith:
      case e: TimeoutException => Future.successful(Left(("Client timeout", e)))
