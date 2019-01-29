import play.sbt.PlayScala

name := "wearbal"

version := "2.6.0-SNAPSHOT"

scalaVersion := "2.12.6"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

enablePlugins(DockerPlugin)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += ws

libraryDependencies += "com.h2database" % "h2" % "1.4.194"
libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.16"

libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test


javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

evictionWarningOptions in update := EvictionWarningOptions.default
  .withWarnTransitiveEvictions(false)
  .withWarnDirectEvictions(false)