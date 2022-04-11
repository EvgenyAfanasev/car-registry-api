package ru.afanasev.embedika.registry.domain.auth.repository.model

final case class TokenEntity(
    jwt: String,
    refresh: String,
    userId: Long,
)

object TokenEntity {
  def unknown: TokenEntity =
    throw new RuntimeException("unproccessable entity")
}
