scalaVersion := "2.13.8"
name := "registry-api"
organization := "ru.afanasev.embedika"
version := "1.0"

val redisVersion   = "1.1.1"
val configVersion  = "0.17.1"
val doobieVersion  = "1.0.0-RC2"
val circeVersion   = "0.15.0-M1"
val http4sVersion  = "0.23.11"
val flywayVersion  = "8.5.7"
val jwtVersion     = "9.0.5"
val logVersion     = "2.2.0"
val loggingVersion = "3.9.4"
val logbackVersion = "1.2.10"

libraryDependencies += "org.http4s"                 %% "http4s-dsl"          % http4sVersion
libraryDependencies += "org.http4s"                 %% "http4s-circe"        % http4sVersion
libraryDependencies += "org.http4s"                 %% "http4s-blaze-server" % http4sVersion
libraryDependencies += "io.circe"                   %% "circe-generic"       % circeVersion
libraryDependencies += "io.circe"                   %% "circe-refined"       % circeVersion
libraryDependencies += "io.circe"                   %% "circe-parser"        % circeVersion
libraryDependencies += "org.tpolecat"               %% "doobie-core"         % doobieVersion
libraryDependencies += "org.tpolecat"               %% "doobie-postgres"     % doobieVersion
libraryDependencies += "org.tpolecat"               %% "doobie-hikari"       % doobieVersion
libraryDependencies += "com.github.pureconfig"      %% "pureconfig"          % configVersion
libraryDependencies += "dev.profunktor"             %% "redis4cats-effects"  % redisVersion
libraryDependencies += "org.flywaydb"               % "flyway-core"          % flywayVersion
libraryDependencies += "com.github.jwt-scala"       %% "jwt-core"            % jwtVersion
libraryDependencies += "com.github.jwt-scala"       %% "jwt-circe"           % jwtVersion
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"       % loggingVersion
libraryDependencies += "ch.qos.logback"             % "logback-classic"      % logbackVersion

assemblyJarName in assembly := "registry.jar"
assemblyMergeStrategy in assembly := {
 case PathList("META-INF", "services", xs@_*) => MergeStrategy.filterDistinctLines
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}