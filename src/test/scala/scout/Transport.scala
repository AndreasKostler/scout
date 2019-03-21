package com.scout

import cats.effect.IO

import org.http4s._
import com.scout.domain._
import org.http4s.circe.CirceEntityEncoder._
import io.circe.generic.auto._
import io.circe.refined._

import com.scout.dto.CarRequest

trait Transport {
  def get(params: (String, String)*) = {
    val uri = params.toList.foldLeft(root) {
      case (uri, (k, v)) => uri.withQueryParam(k, v)
    }
    Request[IO](
      method = Method.GET,
      uri = uri
    )
  }

  def put(id: Car.Id, req: CarRequest) =
    Request[IO](
      method = Method.PUT,
      uri = root.withQueryParam("carId", id.value)
    ).withEntity(req)

  def delete(id: Car.Id) =
    Request[IO](
      method = Method.DELETE,
      uri = root.withQueryParam("carId", id.value)
    )

  def post(car: CarRequest) =
    Request[IO](
      method = Method.POST,
      uri = root
    ).withEntity(car)

  def fire(router: Router[IO], req: Request[IO]) = router.api.run(req)

  val root = Uri.uri("/api/1.0/cars")

}
