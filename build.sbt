import Dependencies._

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.scout"
ThisBuild / organizationName := "scout"

sbtPlugin := true

lazy val root = (project in file("."))
  .settings(
    name := "scout",
    libraryDependencies ++= Seq(
      refined,
      catsCore,
      catsEffect,
      scalaTest % Test,
      scalaCheck % Test,
      scalaCheckShapeless % Test,
      refinedScalaCheck % Test,
      refinedShapeless % Test
    ) ++ circeAll ++ http4sAll
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
