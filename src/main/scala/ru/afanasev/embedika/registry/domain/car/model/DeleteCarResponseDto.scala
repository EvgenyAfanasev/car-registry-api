package ru.afanasev.embedika.registry.domain.car.model

import ru.afanasev.embedika.registry.domain.car.repository.model.CarEntity.ExistingCarEntity

final case class DeleteCarResponseDto(
    car: ExistingCarEntity
)
