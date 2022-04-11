package ru.afanasev.embedika.registry.domain.auth

import cats.implicits._
import cats.data.OptionT
import cats.data.Kleisli
import cats.effect.kernel.Async

import org.typelevel.ci._

import dev.profunktor.redis4cats.algebra.StringCommands

import org.http4s.Request
import org.http4s.server.AuthMiddleware

import ru.afanasev.embedika.registry.domain.auth.model._
import ru.afanasev.embedika.registry.domain.auth.model.SignInResponseDto._
import ru.afanasev.embedika.registry.domain.auth.model.SignUpResponseDto._
import ru.afanasev.embedika.registry.domain.auth.model.RefreshResponseDto._
import ru.afanasev.embedika.registry.domain.auth.repository.model.Roles._
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity.ExistingUserEntity
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity.SecureUserEntity

class AuthService[F[+_]: Async](
    userService: UserService[F],
    tokenService: TokenService[F]
) {

  type Auth[A] = OptionT[F, A]

  private def authUser(roles: Seq[Role]): Kleisli[Auth, Request[F], SecureUserEntity] = Kleisli { request =>
    for {
      header <- OptionT.fromOption[F](request.headers.get(ci"Authorization").map(_.head))
      user   <- tokenService.decodeToken(header.value)
      if roles.contains(user.role)
    } yield user
  }

  def signIn(auth: SignInRequestDto): F[SignInResponseDto] =
    for {
      user <- userService.findUserByUsernameAndPassword(
        auth.username.value,
        auth.password.value
      )
      result <- user match {
        case Some(user) =>
          tokenService
            .encodeToken(SecureUserEntity.secure(user))
            .map(SignInSuccessResponseDto.fromToken)
        case None =>
          SignInIncorrectPasswordResponseDto("incorrect username and/or password")
            .pure[F]
      }
    } yield result

  def refresh(form: RefreshRequestDto) =
    (for {
      token <- OptionT(tokenService.findToken(form.jwt))
      if token.refresh == form.refresh
      user <- OptionT(userService.findById(token.userId))
      refreshedToken <- OptionT.liftF(
        tokenService.refreshToken(
          form.jwt,
          SecureUserEntity.secure(user)
        )
      )
    } yield refreshedToken).value.map(_ match {
      case Some(result) =>
        RefreshSuccessResponseDto(
          result.jwt,
          result.refresh
        )
      case None =>
        RefreshFailedResponseDto(
          "incorrect jwt and/or refresh token"
        )
    })

  def signUp(form: SignUpRequestDto): F[SignUpResponseDto] =
    for {
      existingUser <- userService.findByUsername(form.username.value)
      result <- existingUser match {
        case Some(user) =>
          SignUpAlreadyExistsResponseDto(s"user with username ${user.username} already exists")
            .pure[F]
        case None =>
          userService
            .createUser(form.username.value, form.password.value)
            .map(SignUpSuccessResponseDto)
      }
    } yield result

  val authMiddleware = (roles: Seq[Role]) => AuthMiddleware(authUser(roles))
}

object AuthService {
  def apply[F[+_]: Async](
      userService: UserService[F],
      tokenService: TokenService[F]
  ) = new AuthService[F](userService, tokenService)
}
