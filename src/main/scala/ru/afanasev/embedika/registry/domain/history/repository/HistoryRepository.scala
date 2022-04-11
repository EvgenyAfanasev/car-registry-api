package ru.afanasev.embedika.registry.domain.history.repository

import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryEntity._
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryResult.Result
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryType.Type
import ru.afanasev.embedika.registry.domain.Page

trait HistoryRepository[F[_]] {
  def save(history: NewHistoryEntity): F[ExistingHistoryEntity]

  def findAll(
    page: Page,
    userId: Option[Long],
    result: Option[Result],
    historyType: Option[Type]
  ): F[Seq[ExistingHistoryEntity]]
}
