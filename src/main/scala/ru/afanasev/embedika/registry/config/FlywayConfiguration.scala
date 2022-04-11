package ru.afanasev.embedika.registry.config

import cats.effect.kernel.Sync

import doobie.hikari.HikariTransactor

import org.flywaydb.core.Flyway

object FlywayConfiguration {

  def migrate[F[_]: Sync](transactor: HikariTransactor[F]) =
    transactor.configure { dataSource =>
      Sync[F].delay {
        Flyway.configure
          .dataSource(dataSource)
          .load
          .migrate
      }
    }
}
