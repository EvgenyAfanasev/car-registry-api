package ru.afanasev.embedika.registry.domain.car

import cats.implicits._
import cats.data.Kleisli
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.effect.kernel.Resource

import org.http4s.Response
import org.http4s.HttpRoutes
import org.http4s.AuthedRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

import io.circe.generic.auto._
import io.circe.refined._

import ru.afanasev.embedika.registry.domain.OrderByQueryParam
import ru.afanasev.embedika.registry.domain.OrderMethodQueryParam
import ru.afanasev.embedika.registry.domain.FromQueryParam
import ru.afanasev.embedika.registry.domain.ToQueryParam
import ru.afanasev.embedika.registry.domain.Page
import ru.afanasev.embedika.registry.domain.Methods
import ru.afanasev.embedika.registry.domain.car.model.CarResponseDto._
import ru.afanasev.embedika.registry.domain.car.model.CarRequestDto
import ru.afanasev.embedika.registry.domain.car.model.DeleteCarResponseDto
import ru.afanasev.embedika.registry.domain.history.HistoryService
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryType._
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryResult._
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity.SecureUserEntity

import com.typesafe.scalalogging.LazyLogging

class CarRoute[F[+_]: Async](
    carService: CarService[F],
    historyService: HistoryService[F]
) extends LazyLogging {

  object NumberQueryParam extends OptionalQueryParamDecoderMatcher[String]("number")

  object YearQueryParam extends OptionalQueryParamDecoderMatcher[Int]("year")

  object ColorQueryParam extends OptionalQueryParamDecoderMatcher[String]("color")

  object ManufactureQueryParam extends OptionalQueryParamDecoderMatcher[String]("manufacture")

  val dsl: Http4sDsl[F] = Http4sDsl.apply[F]
  import dsl._

  def authRequest = AuthedRoutes.of[SecureUserEntity, F] {

    case auth @ POST -> Root as user =>
      for {
        start  <- Sync[F].delay(System.currentTimeMillis)
        car    <- auth.req.as[CarRequestDto]
        result <- carService.addCar(car, user.id)
        end = System.currentTimeMillis - start
        response <- result match {
          case success: CarSuccessResponseDto =>
            Sync[F].delay(
              logger.info("[user: {}] succeessful added car from registry [{} ms]", user.username, end)
            ) *> historyService.save(ADD_CAR, user.id.some, end, SUCCESS) *> Ok(success)
          case error: CarAlreadyExistsResponseDto =>
            Sync[F].delay(
              logger
                .info("[user: {}] can't add new car, car with this number already exists [{} ms]", user.username, end)
            ) *> historyService.save(ADD_CAR, user.id.some, end, ALREADY_EXISTS) *> Conflict(error)
        }
      } yield response

    case DELETE -> Root / id as user =>
      for {
        start  <- Sync[F].delay(System.currentTimeMillis)
        result <- carService.remove(user.id, id.toLong)
        end = System.currentTimeMillis - start
        response <- result match {
          case Some(car) =>
            Sync[F].delay(
              logger.info("[user: {}] succeessful deleted car from registry [{} ms]", user.username, end)
            ) *> historyService.save(DEL_CAR, user.id.some, end, SUCCESS) *> Ok(car)
          case None =>
            Sync[F].delay(
              logger.info("[user: {}] car with id {} can't be found for deleting [{} ms]", user.username, id, end)
            ) *> historyService.save(DEL_CAR, user.id.some, end, SUCCESS) *> NotFound()
        }
      } yield response

    case GET -> Root
          :? NumberQueryParam(number)
            +& ManufactureQueryParam(manufacture)
            +& ColorQueryParam(color)
            +& YearQueryParam(year)
            +& OrderByQueryParam(orderBy)
            +& OrderMethodQueryParam(method)
            +& FromQueryParam(from)
            +& ToQueryParam(to) as user =>
      for {
        start <- Sync[F].delay(System.currentTimeMillis)
        result <- carService.findByUserId(
          Page.create(orderBy, method, from, to),
          user.id,
          color,
          number,
          manufacture,
          year
        )
        end = System.currentTimeMillis - start
        response <- Sync[F].delay(
          logger.info("[user: {}] succeessful got list of user's cars with size {} [{} ms]", user.username, result.size, end)
        ) *> historyService.save(GET_CARS, user.id.some, end, SUCCESS) *> Ok(result)
      } yield response

    case auth @ GET -> Root / "statistics" as user =>
      for {
        start  <- Sync[F].delay(System.currentTimeMillis)
        result <- carService.userStatistics(user.id)
        end = System.currentTimeMillis - start
        response <- Sync[F].delay(
          logger.info("[user: {}] succeessful got user statistics [{} ms]", user.username, end)
        ) *> historyService.save(GET_STAT, user.id.some, end, SUCCESS) *> Ok(result)
      } yield response
  }
}

object CarRoute {
  def apply[F[+_]: Async](
      carService: CarService[F],
      historyService: HistoryService[F]
  ) = new CarRoute[F](carService, historyService)
}
