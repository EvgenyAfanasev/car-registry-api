package ru.afanasev.embedika.registry

import cats.effect._
import cats.implicits._
import cats._

import ru.afanasev.embedika.registry.config.PropertiesConfiguration.loadProperties
import ru.afanasev.embedika.registry.config.DatabaseConfiguration.mkDatabaseConnection
import ru.afanasev.embedika.registry.config.FlywayConfiguration.migrate
import ru.afanasev.embedika.registry.config.RedisConfiguration.mkRedisConnection
import ru.afanasev.embedika.registry.config.PropertiesConfiguration.ApplicationProperties

import ru.afanasev.embedika.registry.domain.history.repository.HistoryRepositoryImpl
import ru.afanasev.embedika.registry.domain.history.HistoryRoute
import ru.afanasev.embedika.registry.domain.history.HistoryService
import ru.afanasev.embedika.registry.domain.auth.repository.TokenRepositoryImpl
import ru.afanasev.embedika.registry.domain.auth.repository.UserRepositoryImpl
import ru.afanasev.embedika.registry.domain.auth.repository.model.Roles._
import ru.afanasev.embedika.registry.domain.auth.TokenService
import ru.afanasev.embedika.registry.domain.auth.AuthService
import ru.afanasev.embedika.registry.domain.auth.UserService
import ru.afanasev.embedika.registry.domain.auth.AuthRoute
import ru.afanasev.embedika.registry.domain.car.repository.CarRepositoryImpl
import ru.afanasev.embedika.registry.domain.car.CarRoute
import ru.afanasev.embedika.registry.domain.car.CarService

import org.http4s.server.blaze._
import org.http4s.implicits._
import org.http4s.server.Router

object Server extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    buildServer[IO].use(_.compile.drain).as(ExitCode.Success)

  def buildServer[F[+_]: Async: Parallel] =
    for {
      properties <- Resource.pure[F, ApplicationProperties](loadProperties)
      transactor <- mkDatabaseConnection(properties.database).evalTap(migrate(_))
      redis      <- mkRedisConnection(properties.redis)
      tokenRepository   = TokenRepositoryImpl[F](redis)
      historyRepoistory = HistoryRepositoryImpl[F](transactor)
      userRepository    = UserRepositoryImpl[F](transactor)
      carRepository     = CarRepositoryImpl[F](transactor)
      historyService    = HistoryService[F](historyRepoistory)
      userService       = UserService[F](userRepository)
      tokenService      = TokenService[F](tokenRepository, properties.auth)
      authService       = AuthService[F](userService, tokenService)
      carService        = CarService[F](carRepository)
      authRoute         = AuthRoute[F](authService, historyService)
      carRoute          = CarRoute[F](carService, historyService)
      historyRoute      = HistoryRoute[F](historyService)
      router            = Router(
        "/auth"    -> authRoute.request,
        "/car"     -> authService.authMiddleware(Seq(USER, ADMIN))(carRoute.authRequest),
        "/history" -> authService.authMiddleware(Seq(ADMIN))(historyRoute.authRequest)
      ).orNotFound
    } yield BlazeServerBuilder[F]
      .withHttpApp(router)
      .bindHttp(port = properties.server.port, host = "0.0.0.0")
      .serve
}
