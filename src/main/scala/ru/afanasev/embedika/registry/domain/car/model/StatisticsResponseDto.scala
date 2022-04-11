package ru.afanasev.embedika.registry.domain.car.model

import java.time.LocalDateTime

final case class StatisticsResponseDto(
    countRows: Long,
    dateFirstAdded: Option[LocalDateTime],
    dateLastAdded: Option[LocalDateTime],
    popularColor: Option[String]
)
