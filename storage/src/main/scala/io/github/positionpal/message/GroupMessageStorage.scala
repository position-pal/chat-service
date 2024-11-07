package io.github.positionpal.message

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

import akka.actor.typed.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.persistence.typed.PersistenceId
import akka.stream.scaladsl.Sink
import io.github.positionpal.group.{GroupEventSourceHandler, Message}
import io.github.positionpal.storage.MessageStorage

class GroupMessageStorage(using system: ActorSystem[?]) extends MessageStorage:

  given ExecutionContext = system.executionContext
  private val readJournal: CassandraReadJournal = PersistenceQuery(system)
    .readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  override def getLastMessages(groupID: String)(n: Int): Seq[String] =
    val persistenceId = PersistenceId(GroupEventSourceHandler.entityKey.name, groupID)

    val futureSeq = readJournal.currentEventsByPersistenceId(persistenceId.id, 0L, Long.MaxValue).mapAsync(1):
      envelope =>
        envelope.event match
          case message: Message => Future.successful(message.text)
          case _ => Future.successful("")
    .take(n).runWith(Sink.seq).map(_.filterNot(_.isEmpty))

    Await.result(futureSeq, 5.seconds)
