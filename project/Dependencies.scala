import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.0"
  lazy val scalaCheckShapeless =
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.0"

  // REFINED -------------------------------------------------------------------
  lazy val refined = "eu.timepit" %% "refined" % Versions.refined

  lazy val refinedScalaCheck =
    "eu.timepit" %% "refined-scalacheck" % Versions.refined

  lazy val refinedShapeless =
    "eu.timepit" %% "refined-shapeless" % Versions.refined

  // CIRCE ---------------------------------------------------------------------
  lazy val circeCore = "io.circe" %% "circe-core" % Versions.circe

  lazy val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe

  lazy val circeRefined = "io.circe" %% "circe-refined" % Versions.circe

  lazy val circeAll = Seq(circeCore, circeGeneric, circeRefined)

  // CATS ----------------------------------------------------------------------
  lazy val catsCore = "org.typelevel" %% "cats-core" % Versions.cats

  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "1.2.0"

  // HTTP4S --------------------------------------------------------------------
  lazy val http4sServer =
    "org.http4s" %% "http4s-blaze-server" % Versions.http4s

  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % Versions.http4s

  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.http4s

  lazy val http4sAll = Seq(http4sDsl, http4sServer, http4sCirce)

  // ---------------------------------------------------------------------------

  object Versions {
    val cats = "1.6.0"
    val http4s = "0.20.0-M6"
    val circe = "0.11.1"
    val refined = "0.9.4"
  }
}
