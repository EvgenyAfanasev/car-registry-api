package ru.afanasev.embedika.registry.domain.car.model

import ru.afanasev.embedika.registry.domain.car.repository.model.CarEntity.ExistingCarEntity

trait CarResponseDto

object CarResponseDto {

  final case class CarSuccessResponseDto(
      car: ExistingCarEntity
  ) extends CarResponseDto

  final case class CarAlreadyExistsResponseDto(
      message: String
  ) extends CarResponseDto
}
