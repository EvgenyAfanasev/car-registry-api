package ru.afanasev.embedika.registry.domain.auth.repository

import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity._

trait UserRepository[F[_]] {

  def findByUsernameAndPassword(username: String, password: String): F[Option[ExistingUserEntity]]

  def findByUsername(username: String): F[Option[ExistingUserEntity]]

  def save(user: NewUserEntity): F[ExistingUserEntity]

  def findById(id: Long): F[Option[ExistingUserEntity]]
}
