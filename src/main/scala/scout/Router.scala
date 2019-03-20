package com.scout

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{Router => Http4sRouter}
import io.circe.generic.auto._
import io.circe.refined._
import org.http4s.circe.CirceEntityDecoder._
//import org.http4s.circe.CirceEntityEncoder._

import com.scout.dto.CarsRequest

// A very simple API gateway'esque router.
class Router[F[_]: Effect] extends Http4sDsl[F] {
  import org.http4s.implicits._
  import cats.implicits._

  def api =
    Http4sRouter[F](
      "/api/1.0" -> cars
    ).orNotFound

  // ---------------------------------------------------------------------------
  private val cars = HttpRoutes.of[F] {
    case req @ POST -> Root / "cars" =>
      req.as[CarsRequest] >>= (_ => NotFound("cars"))

    case GET -> Root / id => NotFound(id)

    case GET -> Root => NotFound("cars")
  }

}
