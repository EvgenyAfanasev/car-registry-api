package ru.afanasev.embedika.registry.domain.auth.repository

import ru.afanasev.embedika.registry.domain.auth.repository.model.TokenEntity
import java.time.Instant
import scala.concurrent.duration._
import cats.implicits._
import cats.effect.kernel.Async
import dev.profunktor.redis4cats.RedisCommands

class TokenRepositoryImpl[F[_]: Async](
    redis: RedisCommands[F, String, TokenEntity]
) extends TokenRepository[F] {

  override def findById(id: String): F[Option[TokenEntity]] = 
    redis.get(id)

  override def save(token: TokenEntity, sessionExpiration: Long): F[TokenEntity] =
    redis
      .setEx(token.jwt, token, sessionExpiration.seconds)
      .as(token)

  override def deleteById(id: String): F[Long] = 
    redis.del(id)
}

object TokenRepositoryImpl {
  def apply[F[_]: Async](
      redis: RedisCommands[F, String, TokenEntity]
  ) = new TokenRepositoryImpl[F](redis)
}
