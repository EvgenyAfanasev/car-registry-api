package ru.afanasev.embedika.registry.domain.car.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.api.Max
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.collection.MaxSize
import java.time.LocalDate
import org.checkerframework.checker.index.qual.NonNegative

final case class CarRequestDto(
    number: CarType.Number,
    manufacture: CarType.Manufacture,
    color: CarType.Color,
    year: CarType.Year
)

object CarType {
    
    type Number = String Refined MatchesRegex["""^[А-Яа-я][А-Яа-я0-9]{5,20}$"""]

    type Year = Int Refined Positive

    type Manufacture = String Refined MaxSize[100]

    type Color = String Refined MaxSize[30]
}
