package ru.afanasev.embedika.registry.domain.auth.model

final case class RefreshRequestDto(
  jwt: String,
  refresh: String
)