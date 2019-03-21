package com.scout

import cats.data.EitherT
import java.util.UUID
import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{Router => Http4sRouter}
import io.circe.generic.auto._
import io.circe.refined._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._

import com.scout.dto.CarRequest
import com.scout.domain.Car
import com.scout.repo.CarRepo

// A very simple API gateway'esque router.
class Router[F[_]: Effect](repo: CarRepo[F]) extends Http4sDsl[F] {
  import org.http4s.implicits._
  import cats.implicits._

  def api =
    Http4sRouter[F](
      "/api/1.0" -> cars
    ).orNotFound

  // ---------------------------------------------------------------------------

  // TODO: Don't return Car directly, map to DTO!!!
  private val cars = HttpRoutes.of[F] {

    case GET -> Root / "cars" :? CarIdQueryParamMatcher(id) =>
      repo
        .get(id)
        .value
        .flatMap(_.fold(err => NotFound(err), car => Ok(car.toDto)))

    case GET -> Root / "cars" :? SortOrderQueryParamMatcher(sortOrder) =>
      repo.getAll(sortOrder.getOrElse(SortOrder.ByCarId)) >>= (
          cars => Ok(cars.map(_.toDto))
      )

    case req @ POST -> Root / "cars" =>
      newId >>= (id => validateRequest(req, id, insertCar(repo)))

    case req @ PUT -> Root / "cars" :? CarIdQueryParamMatcher(id) =>
      validateRequest(req, id, updateCar(repo))

    case DELETE -> Root / "cars" :? CarIdQueryParamMatcher(id) =>
      deleteCar(repo, id)
  }

  // ---------------------------------------------------------------------------
  private def transformError(err: CarRepo.Error): F[Response[F]] =
    err match {
      case CarRepo.NotFound(_) => NotFound(err)
      case _                   => BadRequest(err)
    }

  private def toEmptyResponse(op: EitherT[F, CarRepo.Error, _]) =
    op.semiflatMap(_ => Ok())
      .valueOrF(transformError)

  // private def toResponse[A: Encoder](op: EitherT[F, CarRepo.Error, A]) =
  //   op.semiflatMap(x => Ok(x))
  //     .valueOrF(transformError)

  private def insertCar(repo: CarRepo[F])(car: Car): F[Response[F]] =
    toEmptyResponse(repo.insert(car.id, car))

  private def updateCar(repo: CarRepo[F])(car: Car): F[Response[F]] =
    toEmptyResponse(repo.update(car.id, car))

  private def deleteCar(repo: CarRepo[F], id: Car.Id): F[Response[F]] =
    toEmptyResponse(repo.delete(id))

  private def validateRequest(
      req: Request[F],
      id: Car.Id,
      success: Car => F[Response[F]]
  ): F[Response[F]] = {
    val car: F[Option[Car]] = for {
      carReq <- req.as[CarRequest]
    } yield carReq.toCar(id)

    car.flatMap(
      _.fold(
        BadRequest()
      )(car => success(car))
    )
  }

  private def newId = Effect[F].delay(Car.Id(UUID.randomUUID))

  // ---------------------------------------------------------------------------

  implicit def carIdQueryParamDecoder: QueryParamDecoder[Car.Id] =
    QueryParamDecoder[String].map(Car.Id.unsafeFrom)

  object CarIdQueryParamMatcher
      extends QueryParamDecoderMatcher[Car.Id]("carId")

  implicit def sortOrderQueryParamDecoder: QueryParamDecoder[SortOrder] =
    QueryParamDecoder[String].map(
      s => SortOrder.fromString(s).getOrElse(SortOrder.ByCarId)
    )

  object SortOrderQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[SortOrder]("sortOrder")

}
