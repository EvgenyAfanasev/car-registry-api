package ru.afanasev.embedika.registry.domain.car.repository

import cats.implicits._
import cats.effect.kernel.Async

import doobie.util.transactor.Transactor
import doobie.util.fragments.whereAndOpt
import doobie.implicits._
import doobie.implicits.javatime._

import ru.afanasev.embedika.registry.utils.DoobieUtils.orderByOpt
import ru.afanasev.embedika.registry.domain.car.repository.model.CarEntity._
import ru.afanasev.embedika.registry.domain.Page

import java.time.LocalDateTime

class CarRepositoryImpl[F[_]: Async](
    transactor: Transactor[F]
) extends CarRepository[F] {

  object SQL {
    def findByNumber(number: String) = sql"""
            SELECT * FROM registry.cars
            WHERE number = $number
        """.query[ExistingCarEntity]

    def save(car: NewCarEntity) = sql"""
            INSERT INTO registry.cars(
                number, manufacture, year, user_id, color, date_added
            ) VALUES(
                ${car.number},
                ${car.manufacture},
                ${car.year},
                ${car.userId},
                ${car.color},
                ${LocalDateTime.now}
            )
            RETURNING *
        """.query[ExistingCarEntity]

    def findPopularColor(userId: Long) = sql"""
            SELECT color FROM registry.cars
            WHERE user_id = $userId
            GROUP BY color
            ORDER BY COUNT(*) DESC
            LIMIT 1
        """.query[String]

    def count(userId: Long) = sql"""
            SELECT COUNT(*) FROM registry.cars WHERE user_id = $userId
        """.query[Long]

    def deleteById(id: Long) = sql"""
            DELETE FROM registry.cars
            WHERE id = $id
            RETURNING *
        """.query[ExistingCarEntity]

    def findByIdAndUserId(id: Long, userId: Long) = sql"""
            SELECT * FROM registry.cars
            WHERE id = $id AND user_id = $userId
        """.query[ExistingCarEntity]

    def findByUserId(
        userId: Long,
        page: Page,
        optionColor: Option[String],
        optionNumber: Option[String],
        optionManufacture: Option[String],
        optionYear: Option[Int]
    ) = 
      (fr"SELECT * FROM registry.cars" ++
        whereAndOpt(
          fr"user_id = $userId".some,
          optionColor.map(c => fr"color = $c"),
          optionManufacture.map(m => fr"manufacture = $m"),
          optionYear.map(y => fr"year = $y"),
          optionNumber.map(n => fr"number LIKE %$n%")
        ) ++
        orderByOpt(page.orderBy.map(ord => s"${ord.by} ${ord.method}")) ++
        fr" LIMIT ${page.limit}  OFFSET ${page.offset}")
        .query[ExistingCarEntity]
  }

  override def findByNumber(number: String): F[Option[ExistingCarEntity]] =
    SQL.findByNumber(number).option.transact(transactor)

  override def save(car: NewCarEntity): F[ExistingCarEntity] =
    SQL.save(car).unique.transact(transactor)

  override def findPopularColor(userId: Long): F[Option[String]] =
    SQL.findPopularColor(userId).option.transact(transactor)

  override def count(userId: Long): F[Long] =
    SQL.count(userId).unique.transact(transactor)

  override def findByUserId(
      userId: Long,
      page: Page,
      optionColor: Option[String],
      optionNumber: Option[String],
      optionManufacture: Option[String],
      optionYear: Option[Int]
  ): F[Seq[ExistingCarEntity]] =
    SQL
      .findByUserId(userId, page, optionColor, optionNumber, optionManufacture, optionYear)
      .to[Seq]
      .transact(transactor)

  override def deleteById(id: Long): F[ExistingCarEntity] =
    SQL.deleteById(id).unique.transact(transactor)

  override def findByIdAndUserId(id: Long, userId: Long): F[Option[ExistingCarEntity]] =
    SQL.findByIdAndUserId(id, userId).option.transact(transactor)

}

object CarRepositoryImpl {
  def apply[F[_]: Async](
      transactor: Transactor[F]
  ) = new CarRepositoryImpl[F](transactor)
}
