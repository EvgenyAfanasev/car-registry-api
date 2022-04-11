package ru.afanasev.embedika.registry.domain.auth.model

import ru.afanasev.embedika.registry.domain.auth.repository.model.TokenEntity

trait SignInResponseDto

object SignInResponseDto {

  final case class SignInSuccessResponseDto(
      jwt: String,
      refresh: String
  ) extends SignInResponseDto

  object SignInSuccessResponseDto {
    def fromToken(token: TokenEntity) =
      SignInSuccessResponseDto(token.jwt, token.refresh)
  }

  final case class SignInIncorrectPasswordResponseDto(
      message: String
  ) extends SignInResponseDto
}
