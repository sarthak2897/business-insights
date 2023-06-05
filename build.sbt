import play.core.PlayVersion.akkaVersion

name := """business_insights"""
organization := "sarthak"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, LauncherJarPlugin)

scalaVersion := "2.13.11"

lazy val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)

lazy val mongoDependencies = Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "1.0.10-play28",
  "org.reactivemongo" %% "reactivemongo-bson-compat" % "0.20.13",
  "com.typesafe.play" %% "play-json-joda" % "2.7.4")

lazy val kafkaDependency = "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.0"

libraryDependencies += kafkaDependency
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= (akkaDependencies ++ mongoDependencies)
