package ru.afanasev.embedika.registry.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object PropertiesConfiguration {

  def loadProperties =
    ConfigSource.default
      .loadOrThrow[ApplicationProperties]

  final case class ApplicationProperties(
      database: DatabaseProperties,
      auth: AuthProperties,
      redis: RedisProperties,
      server: ServerProperties
  )

  final case class DatabaseProperties(
      url: String,
      packageClassName: String,
      poolSize: Int,
      username: String,
      password: String
  )

  final case class RedisProperties(
      url: String,
      password: Option[String]
  )

  final case class AuthProperties(
      secret: String,
      expirationSeconds: Long,
      sessionExpirationSeconds: Long
  )

  final case class ServerProperties(
      port: Int
  )
}
