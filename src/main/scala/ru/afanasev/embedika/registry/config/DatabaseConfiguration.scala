package ru.afanasev.embedika.registry.config

import cats.effect.kernel.Async

import doobie.util.ExecutionContexts
import doobie.hikari.HikariTransactor

import PropertiesConfiguration.DatabaseProperties

object DatabaseConfiguration {

  def mkDatabaseConnection[F[_]: Async](properties: DatabaseProperties) =
    for {
      pool <- ExecutionContexts.fixedThreadPool[F](properties.poolSize)
      transactor <- HikariTransactor.newHikariTransactor[F](
        properties.packageClassName,
        properties.url,
        properties.username,
        properties.password,
        pool
      )
    } yield transactor
}
