package ru.afanasev.embedika.registry.domain.auth.model

import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity.ExistingUserEntity

trait SignUpResponseDto

object SignUpResponseDto {

  final case class SignUpSuccessResponseDto(
      user: ExistingUserEntity
  ) extends SignUpResponseDto

  final case class SignUpAlreadyExistsResponseDto(
      message: String
  ) extends SignUpResponseDto
}
