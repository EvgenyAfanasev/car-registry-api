package ru.afanasev.embedika.registry.domain.car.repository

import ru.afanasev.embedika.registry.domain.car.repository.model.CarEntity._
import ru.afanasev.embedika.registry.domain.Page

trait CarRepository[F[_]] {

  def findByNumber(number: String): F[Option[ExistingCarEntity]]

  def save(car: NewCarEntity): F[ExistingCarEntity]

  def findPopularColor(userId: Long): F[Option[String]]

  def count(userId: Long): F[Long]

  def findByUserId(
      userId: Long,
      page: Page,
      optionColor: Option[String] = None,
      optionNumber: Option[String] = None,
      optionManufacture: Option[String] = None,
      optionYear: Option[Int] = None
  ): F[Seq[ExistingCarEntity]]

  def deleteById(id: Long): F[ExistingCarEntity]

  def findByIdAndUserId(id: Long, userId: Long): F[Option[ExistingCarEntity]]

}
