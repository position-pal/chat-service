package io.github.positionpal.message

import scala.concurrent.{ExecutionContext, Future}

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

  override def getLastMessages(groupID: String)(n: Int): Future[Seq[String]] =
    val persistenceId = PersistenceId(GroupEventSourceHandler.entityKey.name, groupID)

    readJournal.currentEventsByPersistenceId(persistenceId.id, 0L, Long.MaxValue).collect: envelope =>
      envelope.event match
        case message: Message => message.text
    .runWith(Sink.seq).map(_.takeRight(n))
