package ru.afanasev.embedika.registry.domain.car

import cats.Parallel
import cats.implicits._
import cats.data.OptionT
import cats.data.Kleisli
import cats.effect.kernel.Async

import ru.afanasev.embedika.registry.domain.car.model._
import ru.afanasev.embedika.registry.domain.car.model.CarResponseDto._
import ru.afanasev.embedika.registry.domain.car.repository.CarRepository
import ru.afanasev.embedika.registry.domain.car.repository.model.CarEntity._
import ru.afanasev.embedika.registry.domain.Page
import ru.afanasev.embedika.registry.domain.Ordering
import ru.afanasev.embedika.registry.domain.Methods

class CarService[F[+_]: Async: Parallel](
    carRepository: CarRepository[F]
) {

  def addCar(car: CarRequestDto, userId: Long): F[CarResponseDto] =
    for {
      existingCar <- findByNumber(car.number.value)
      result <- existingCar match {
        case Some(car) =>
          CarAlreadyExistsResponseDto(s"car with number ${car.number} already exists")
            .pure[F]
        case None =>
          save(NewCarEntity.fromRequest(car, userId))
            .map(CarSuccessResponseDto(_))
      }
    } yield result

  def remove(userId: Long, carId: Long): F[Option[DeleteCarResponseDto]] =
    (for {
      existingCar <- OptionT(findByIdAndUserId(userId, carId))
      removedCar  <- OptionT.liftF(deleteById(existingCar.id))
      result      <- OptionT.pure[F](DeleteCarResponseDto(removedCar))
    } yield result).value

  def userStatistics(userId: Long): F[StatisticsResponseDto] =
    (count, findFirstRow, findLastRow, findPopularColor)
      .parMapN { (count, firstRow, lastRow, color) =>
        StatisticsResponseDto(
          count,
          firstRow.map(_.dateAdded),
          lastRow.map(_.dateAdded),
          color
        )
      }
      .run(userId)

  def findByUserId(
      page: Page,
      userId: Long,
      optionColor: Option[String] = None,
      optionNumber: Option[String] = None,
      optionManufacture: Option[String] = None,
      optionYear: Option[Int] = None
  ): F[Seq[ExistingCarEntity]] = carRepository.findByUserId(
    userId,
    page,
    optionColor,
    optionNumber,
    optionManufacture,
    optionYear
  )

  private def count: Kleisli[F, Long, Long] =
    Kleisli(carRepository.count)

  private def findLastRow: Kleisli[F, Long, Option[ExistingCarEntity]] =
    Kleisli(findTailRow(_, Methods.DESC))

  private def findFirstRow: Kleisli[F, Long, Option[ExistingCarEntity]] =
    Kleisli(findTailRow(_, Methods.ASC))

  private def findPopularColor: Kleisli[F, Long, Option[String]] =
    Kleisli(carRepository.findPopularColor)

  private def findTailRow(userId: Long, method: Methods.Method) =
    findByUserId(Page(orderBy = Ordering("date_added", method).some, limit = 1), userId)
      .map(_.headOption)

  private def deleteById(carId: Long): F[ExistingCarEntity] =
    carRepository.deleteById(carId)

  private def findByIdAndUserId(userId: Long, carId: Long): F[Option[ExistingCarEntity]] =
    carRepository.findByIdAndUserId(carId, userId)

  private def findByNumber(number: String): F[Option[ExistingCarEntity]] =
    carRepository.findByNumber(number)

  private def save(car: NewCarEntity): F[ExistingCarEntity] =
    carRepository.save(car)
}

object CarService {
  def apply[F[+_]: Async: Parallel](
      carRepository: CarRepository[F]
  ) = new CarService[F](carRepository)
}
