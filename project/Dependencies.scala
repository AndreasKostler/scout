import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  lazy val circeCore = "io.circe" %% "circe-core" % Versions.circe

  lazy val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe

  lazy val circeRefined = "io.circe" %% "circe-refined" % Versions.circe

  lazy val circeAll = Seq(circeCore, circeGeneric, circeRefined)

  lazy val refined = "eu.timepit" %% "refined" % "0.9.4"

  // ---------------------------------------------------------------------------

  object Versions {
    val circe = "0.11.1"
  }
}
