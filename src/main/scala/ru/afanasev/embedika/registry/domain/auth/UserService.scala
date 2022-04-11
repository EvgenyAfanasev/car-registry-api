package ru.afanasev.embedika.registry.domain.auth

import cats.effect.kernel.Async

import ru.afanasev.embedika.registry.domain.auth.repository.UserRepository
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity._
import ru.afanasev.embedika.registry.domain.auth.repository.model.Roles

import java.time.LocalDateTime

class UserService[F[_]](
    userRepository: UserRepository[F]
) {

  def findUserByUsernameAndPassword(
      username: String,
      password: String
  ): F[Option[ExistingUserEntity]] = 
    userRepository.findByUsernameAndPassword(username, password)

  def findByUsername(username: String): F[Option[ExistingUserEntity]] = 
    userRepository.findByUsername(username)

  def findById(id: Long) = 
    userRepository.findById(id)


  def createUser(
      username: String,
      password: String
  ): F[ExistingUserEntity] = userRepository.save(
    NewUserEntity(username, password, Roles.USER)
  )
}

object UserService {
  def apply[F[_]: Async](
      userRepository: UserRepository[F]
  ) = new UserService[F](userRepository)
}
