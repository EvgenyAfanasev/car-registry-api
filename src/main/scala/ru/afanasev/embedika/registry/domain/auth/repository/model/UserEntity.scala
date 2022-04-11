package ru.afanasev.embedika.registry.domain.auth.repository.model

import io.circe.Encoder
import io.circe.Decoder

import doobie.postgres.implicits._

import java.time.LocalDateTime

trait UserEntity

object UserEntity {

  import Roles.Role

  final case class NewUserEntity(
      username: String,
      password: String,
      role: Role,
  ) extends UserEntity

  final case class ExistingUserEntity(
      id: Long,
      username: String,
      password: String,
      role: Role,
      dateAdded: LocalDateTime
  ) extends UserEntity

  final case class SecureUserEntity(
      id: Long,
      username: String,
      role: Role,
  ) extends UserEntity

  object SecureUserEntity {
    def secure(user: ExistingUserEntity) = 
      SecureUserEntity(user.id, user.username, user.role)
  }
}

object Roles extends Enumeration {

  type Role = Value

  val USER  = Value("USER")
  val ADMIN = Value("ADMIN")

  implicit val genderDecoder: Decoder[Roles.Value] =
    Decoder.decodeEnumeration(Roles)
  implicit val genderEncoder: Encoder[Roles.Value] =
    Encoder.encodeEnumeration(Roles)

  implicit val rolePgEnum = pgEnum(Roles, "application_role")
}
