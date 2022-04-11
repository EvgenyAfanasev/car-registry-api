package ru.afanasev.embedika.registry.domain.history

import cats.implicits._
import cats.effect.kernel.Async

import ru.afanasev.embedika.registry.domain.history.repository.HistoryRepository
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryEntity._
import ru.afanasev.embedika.registry.domain.history.model.HistoryResponseDto
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryResult.Result
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryType.Type
import ru.afanasev.embedika.registry.domain.Page

class HistoryService[F[_]: Async](
    historyRepoistory: HistoryRepository[F]
) {

  def findHistory(
      page: Page,
      userId: Option[Long],
      result: Option[Result],
      historyType: Option[Type]
  ) =
    historyRepoistory
      .findAll(page, userId, result, historyType)
      .map(_.map(HistoryResponseDto.fromEntity(_)))

  def save(
      historyType: Type,
      userId: Option[Long],
      executionTime: Long,
      result: Result
  ) =
    historyRepoistory
      .save(NewHistoryEntity(historyType, userId, result, executionTime))
      .map(HistoryResponseDto.fromEntity(_))

}

object HistoryService {
  def apply[F[_]: Async](
      historyRepoistory: HistoryRepository[F]
  ) = new HistoryService[F](historyRepoistory)
}
