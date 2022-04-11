package ru.afanasev.embedika.registry.domain.auth.repository

import ru.afanasev.embedika.registry.domain.auth.repository.model.TokenEntity

trait TokenRepository[F[_]] {
  def findById(id: String): F[Option[TokenEntity]]

  def save(token: TokenEntity, sessionExpiration: Long): F[TokenEntity]

  def deleteById(id: String): F[Long]
}
