package ru.afanasev.embedika.registry.domain.car.repository.model

import ru.afanasev.embedika.registry.domain.car.model.CarRequestDto

import java.time.LocalDateTime

trait CarEntity

object CarEntity {

  final case class NewCarEntity(
      number: String,
      manufacture: String,
      year: Int,
      userId: Long,
      color: String
  ) extends CarEntity

  final case class ExistingCarEntity(
      id: Long,
      number: String,
      manufacture: String,
      year: Int,
      userId: Long,
      color: String,
      dateAdded: LocalDateTime
  ) extends CarEntity

  object NewCarEntity {
    def fromRequest(car: CarRequestDto, userId: Long) =
      NewCarEntity(car.number.value, car.manufacture.value, car.year.value, userId, car.color.value)
  }
}
