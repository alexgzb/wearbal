import play.sbt.PlayScala

name := "wearbal"

version := "2.6.0-SNAPSHOT"

scalaVersion := "2.12.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += ws

libraryDependencies += "com.h2database" % "h2" % "1.4.194"
libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.16"

libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test


evictionWarningOptions in update := EvictionWarningOptions.default
  .withWarnTransitiveEvictions(false)
  .withWarnDirectEvictions(false)