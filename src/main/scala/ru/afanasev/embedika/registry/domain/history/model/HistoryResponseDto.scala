package ru.afanasev.embedika.registry.domain.history.model

import java.time.LocalDateTime

import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryType
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryEntity.ExistingHistoryEntity

final case class HistoryResponseDto(
    id: Long,
    historyType: HistoryType.Type,
    userId: Option[Long],
    executionTime: Long,
    dateAdded: LocalDateTime
)

object HistoryResponseDto {
  def fromEntity(entity: ExistingHistoryEntity) =
    HistoryResponseDto(
      entity.id,
      entity.historyType,
      entity.userId,
      entity.executionTime,
      entity.dateAdded
    )
}
