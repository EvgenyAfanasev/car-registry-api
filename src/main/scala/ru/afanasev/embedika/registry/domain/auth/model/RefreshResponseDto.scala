package ru.afanasev.embedika.registry.domain.auth.model

import ru.afanasev.embedika.registry.domain.auth.repository.model.TokenEntity

sealed trait RefreshResponseDto

object RefreshResponseDto {
  final case class RefreshSuccessResponseDto(
    jwt: String,
    refresh: String
  ) extends RefreshResponseDto

  object RefreshSuccessResponseDto {
    def fromEntity(entity: TokenEntity) = 
      RefreshSuccessResponseDto(entity.jwt, entity.refresh)
  }

  final case class RefreshFailedResponseDto(
    message: String
  ) extends RefreshResponseDto
}



