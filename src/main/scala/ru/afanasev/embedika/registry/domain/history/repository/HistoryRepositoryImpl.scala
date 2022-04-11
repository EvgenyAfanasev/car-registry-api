package ru.afanasev.embedika.registry.domain.history.repository

import cats.implicits._
import cats.effect.kernel.Async

import doobie.util.transactor.Transactor
import doobie.util.fragments.whereAndOpt

import doobie.implicits._
import doobie.implicits.javatime._

import ru.afanasev.embedika.registry.domain.Page
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryEntity._
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryResult.Result
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryType.Type

import java.time.LocalDateTime

class HistoryRepositoryImpl[F[_]: Async](
    transactor: Transactor[F]
) extends HistoryRepository[F] {

  object SQL {

    def findAll(
        page: Page,
        userId: Option[Long],
        result: Option[Result],
        historyType: Option[Type]
    ) =
      (fr"SELECT * FROM history.history " ++
        whereAndOpt {
          userId.map(ui => fr"user_id = $ui")
          result.map(r => fr"result = $r")
          historyType.map(ht => fr"history_type = $ht")
        } ++
        fr"ORDER BY ${page.ordering}" ++
        fr" OFFSET ${page.offset} LIMIT ${page.limit}").query[ExistingHistoryEntity]

    def save(history: NewHistoryEntity) = sql"""
        INSERT INTO history.history(
            history_type, user_id, result, execution_time, date_added
        ) VALUES(
            ${history.historyType}, 
            ${history.userId}, 
            ${history.result}, 
            ${history.executionTime},
            ${LocalDateTime.now}
        )
        RETURNING *
    """.query[ExistingHistoryEntity]
  }

  override def save(history: NewHistoryEntity): F[ExistingHistoryEntity] =
    SQL.save(history).unique.transact(transactor)

  override def findAll(
      page: Page,
      userId: Option[Long],
      result: Option[Result],
      historyType: Option[Type]
  ): F[Seq[ExistingHistoryEntity]] =
    SQL.findAll(page, userId, result, historyType).to[Seq].transact(transactor)
}

object HistoryRepositoryImpl {
  def apply[F[_]: Async](
      transactor: Transactor[F]
  ) = new HistoryRepositoryImpl[F](transactor)
}
