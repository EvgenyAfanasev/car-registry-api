package ru.afanasev.embedika.registry.domain.history

import cats.implicits._
import cats.effect.kernel.Async
import cats.effect.kernel.Sync

import org.http4s.HttpRoutes
import org.http4s.AuthedRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

import io.circe.generic.auto._

import ru.afanasev.embedika.registry.domain.Page
import ru.afanasev.embedika.registry.domain.OrderByQueryParam
import ru.afanasev.embedika.registry.domain.OrderMethodQueryParam
import ru.afanasev.embedika.registry.domain.FromQueryParam
import ru.afanasev.embedika.registry.domain.ToQueryParam
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryResult.Result
import ru.afanasev.embedika.registry.domain.history.repository.model.HistoryType.Type
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity.SecureUserEntity

import com.typesafe.scalalogging.LazyLogging

class HistoryRoute[F[_]: Async](
    historyService: HistoryService[F]
) extends LazyLogging {

  object UserIdQueryParam extends OptionalQueryParamDecoderMatcher[Long]("userId")

  object ResultQueryParam extends OptionalQueryParamDecoderMatcher[Result]("result")

  object TypeQueryParam extends OptionalQueryParamDecoderMatcher[Type]("type")

  val dsl: Http4sDsl[F] = Http4sDsl.apply[F]
  import dsl._

  def authRequest = AuthedRoutes.of[SecureUserEntity, F] {
    case GET -> Root
          :? UserIdQueryParam(userId)
            +& ResultQueryParam(result)
            +& TypeQueryParam(historyType)
            +& OrderByQueryParam(orderBy)
            +& OrderMethodQueryParam(method)
            +& FromQueryParam(from)
            +& ToQueryParam(to) as user =>
      for {
        start  <- Sync[F].delay(System.currentTimeMillis)
        result <- historyService.findHistory(
          Page.create(orderBy, method, from, to),
          userId,
          result,
          historyType
        )
        end    =  System.currentTimeMillis - start 
        response <- Sync[F].delay(
          logger.info(
            "[user: {}] got history with parameters userId = {}, result = {}, order by {}, method = {} [{} ms]",
            user.id, userId, result, orderBy, method, end
          )
        ) *> Ok(result)
      } yield response
  }
}

object HistoryRoute {
  def apply[F[_]: Async](
      historyService: HistoryService[F]
  ) = new HistoryRoute[F](historyService)
}
