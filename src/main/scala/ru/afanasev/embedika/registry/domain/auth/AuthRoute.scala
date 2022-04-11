package ru.afanasev.embedika.registry.domain.auth


import cats.Parallel
import cats.implicits._
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.effect.kernel.Resource

import org.http4s.HttpRoutes
import org.http4s.AuthedRoutes
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._

import io.circe.generic.auto._
import io.circe.refined._

import ru.afanasev.embedika.registry.domain.auth.model.SignInResponseDto
import ru.afanasev.embedika.registry.domain.auth.model.SignInRequestDto
import ru.afanasev.embedika.registry.domain.auth.model.SignUpRequestDto
import ru.afanasev.embedika.registry.domain.auth.model.SignUpResponseDto._
import ru.afanasev.embedika.registry.domain.auth.model.SignInResponseDto._
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity
import ru.afanasev.embedika.registry.domain.history.HistoryService
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryType._
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryResult._
import ru.afanasev.embedika.registry.domain.auth.model.RefreshRequestDto
import ru.afanasev.embedika.registry.domain.auth.model.RefreshResponseDto._

import com.typesafe.scalalogging.LazyLogging

class AuthRoute[F[+_]: Async: Parallel](
    authService: AuthService[F],
    historyService: HistoryService[F]
) extends LazyLogging {

  val dsl: Http4sDsl[F] = Http4sDsl.apply[F]
  import dsl._

  def request: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "sign-up" =>
      for {
        start  <- Sync[F].delay(System.currentTimeMillis)
        form   <- req.as[SignUpRequestDto]
        result <- authService.signUp(form)
        end = System.currentTimeMillis - start
        response <- result match {
          case success: SignUpSuccessResponseDto =>
            Sync[F].delay(
              logger.info("[user: {}] successful sign up to service [{} ms]", form.username, end)
            ) *> historyService.save(SIGN_UP, None, end, SUCCESS) *> Ok(success)
          case error: SignUpAlreadyExistsResponseDto =>
            Sync[F].delay(
              logger.info("[user: {}] user already exists [{} ms]", form.username, end)
            ) *> historyService.save(SIGN_UP, None, end, ALREADY_EXISTS) *> Conflict(error)
        }
      } yield response
    case req @ POST -> Root / "sign-in" =>
      for {
        form <- req.as[SignInRequestDto]
        start = System.currentTimeMillis
        result <- authService.signIn(form)
        end = System.currentTimeMillis - start
        response <- result match {
          case error: SignInIncorrectPasswordResponseDto =>
            Sync[F].delay(
              logger.info("[user: {}] incorrect password [{} ms]", form.username, end)
            ) *> historyService.save(SIGN_IN, None, end, FAILED) *> Forbidden(error)
          case success: SignInSuccessResponseDto =>
            Sync[F].delay(
              logger.info("[user: {}] successful sign in to service [{} ms]", form.username, end)
            ) *> historyService.save(SIGN_IN, None, end, SUCCESS) *> Ok(success)
        }
      } yield response
    case req @ PUT -> Root / "refresh" =>
      for {
        form <- req.as[RefreshRequestDto]
        start = System.currentTimeMillis
        result <- authService.refresh(form)
        end = System.currentTimeMillis - start
        response <- result match {
          case success: RefreshSuccessResponseDto =>
            Sync[F].delay(
              logger.info("successfull refresh token [{} ms]", end)
            ) *> historyService.save(REFRESH, None, end, SUCCESS) *> Ok(success)
          case error: RefreshFailedResponseDto =>
            Sync[F].delay(
              logger.info("jwt can not be refreshed [{} ms]", end)
            ) *> historyService.save(REFRESH, None, end, FAILED) *> Forbidden(error)
        }
      } yield response
  }
}

object AuthRoute {
  def apply[F[+_]: Async: Parallel](
      authService: AuthService[F],
      historyService: HistoryService[F]
  ) = new AuthRoute[F](authService, historyService)
}
