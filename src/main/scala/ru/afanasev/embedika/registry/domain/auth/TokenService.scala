package ru.afanasev.embedika.registry.domain.auth

import cats.implicits._
import cats.data.OptionT
import cats.effect.kernel.Async
import cats.effect.kernel.Sync

import pdi.jwt.JwtCirce
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtClaim

import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.Random
import java.time.Instant

import ru.afanasev.embedika.registry.config.PropertiesConfiguration.AuthProperties
import ru.afanasev.embedika.registry.domain.auth.repository.TokenRepository
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity._
import ru.afanasev.embedika.registry.domain.auth.repository.model.TokenEntity

import scala.util.Try

class TokenService[F[_]: Async](
    tokenRepository: TokenRepository[F],
    cfg: AuthProperties
) {

  private val algorithm = JwtAlgorithm.HMD5

  def decodeToken(jwt: String) =
    for {
      token <- OptionT(tokenRepository.findById(jwt))
      user  <- OptionT.fromOption[F](decodeJwt(jwt))
    } yield user

  def encodeToken(user: SecureUserEntity): F[TokenEntity] =
    saveToken(TokenEntity(encodeJwt(user), generateRefreshToken, user.id))

  def refreshToken(jwt: String, user: SecureUserEntity) =
    for {
      _     <- tokenRepository.deleteById(jwt)
      token <- encodeToken(user)
    } yield token

  def findToken(jwt: String) =
    tokenRepository.findById(jwt)

  def encodeJwt(user: SecureUserEntity) = {
    val claims = JwtClaim(
      expiration = Instant.now
        .plusSeconds(cfg.expirationSeconds)
        .getEpochSecond
        .some,
      issuedAt = Instant.now.getEpochSecond.some,
      subject = user.asJson.noSpaces.some
    )
    JwtCirce.encode(claims, cfg.secret, algorithm)
  }

  private def saveToken(token: TokenEntity): F[TokenEntity] =
    tokenRepository.save(token, cfg.sessionExpirationSeconds)

  private def decodeJwt(jwt: String): Option[SecureUserEntity] =
    for {
      claims <- JwtCirce
        .decode(
          token = jwt,
          key = cfg.secret,
          algorithms = Seq(algorithm)
        ).toOption
      subject <- claims.subject
      user    <- decode[SecureUserEntity](subject).toOption
    } yield user

  private def generateRefreshToken: String =
    Random.alphanumeric
      .take(20)
      .mkString
}

object TokenService {
  def apply[F[_]: Async](
      tokenRepository: TokenRepository[F],
      cfg: AuthProperties
  ) = new TokenService[F](tokenRepository, cfg)
}
