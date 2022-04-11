package ru.afanasev.embedika.registry.config

import cats.implicits._
import cats.effect.kernel.Resource
import cats.effect.kernel.Async

import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.connection._
import dev.profunktor.redis4cats.effect.Log.Stdout._

import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._

import dev.profunktor.redis4cats.codecs.splits.SplitEpi
import dev.profunktor.redis4cats.codecs.Codecs

import PropertiesConfiguration._
import ru.afanasev.embedika.registry.domain.auth.repository.model.TokenEntity

object RedisConfiguration {

  val tokenSplitEpi: SplitEpi[String, TokenEntity] =
    SplitEpi[String, TokenEntity](
      str => decode[TokenEntity](str).getOrElse(TokenEntity.unknown),
      _.asJson.noSpaces
    )

  private val tokenCodec: RedisCodec[String, TokenEntity] =
    Codecs.derive(RedisCodec.Utf8, tokenSplitEpi)

  private def mkUri(cfg: RedisProperties) =
    "redis://" + cfg.password.map(_ + "@").getOrElse("") + cfg.url

  def mkRedisConnection[F[_]: Async](cfg: RedisProperties) =
    for {
      uri    <- Resource.eval(RedisURI.make[F](mkUri(cfg)))
      client <- RedisClient[F].fromUri(uri)
      redis  <- Redis[F].fromClient(client, tokenCodec)
    } yield redis

}
