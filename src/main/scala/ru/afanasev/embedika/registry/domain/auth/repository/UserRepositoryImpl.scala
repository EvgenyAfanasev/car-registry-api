package ru.afanasev.embedika.registry.domain.auth.repository

import cats.implicits._

import doobie.util.transactor.Transactor
import ru.afanasev.embedika.registry.domain.auth.repository.model.UserEntity._
import java.time.LocalDateTime

import doobie.implicits._
import doobie.implicits.javatime._
import cats.effect.kernel.Async

class UserRepositoryImpl[F[_]: Async](
    transactor: Transactor[F]
) extends UserRepository[F] {

  object SQL {
    def save(user: NewUserEntity) = 
      sql"""
        INSERT INTO auth.users(
          username, password, role, date_added
        ) VALUES(
          ${user.username},
          ${user.password},
          ${user.role},
          ${LocalDateTime.now}
        )
        RETURNING *
      """.query[ExistingUserEntity]

    def findById(id: Long) = sql"""
      SELECT * FROM auth.users
      WHERE id = $id
    """.query[ExistingUserEntity]

    def findByUsername(username: String) =
      sql"""
      SELECT * FROM auth.users
      WHERE username = $username
      """.query[ExistingUserEntity]

    def findByUsernameAndPassword(username: String, password: String) =
      sql"""
      SELECT * FROM auth.users
      WHERE username = $username AND password = $password
      """.query[ExistingUserEntity]
  }

  override def findByUsernameAndPassword(username: String, password: String): F[Option[ExistingUserEntity]] = 
    SQL.findByUsernameAndPassword(username, password).option.transact(transactor)

  override def save(user: NewUserEntity): F[ExistingUserEntity] = 
    SQL.save(user).unique.transact(transactor)

  override def findByUsername(username: String): F[Option[ExistingUserEntity]] = 
    SQL.findByUsername(username).option.transact(transactor)

  override def findById(id: Long): F[Option[ExistingUserEntity]] = 
    SQL.findById(id).option.transact(transactor)
}

object UserRepositoryImpl {
  def apply[F[_]: Async](
      transactor: Transactor[F]
  ) = new UserRepositoryImpl[F](transactor)
}
