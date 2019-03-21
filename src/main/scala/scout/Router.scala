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
import org.http4s.server.middleware._
import scala.concurrent.duration._

import com.scout.dto.CarRequest
import com.scout.domain.Car
import com.scout.repo.CarRepo

// A very simple API gateway'esque router.
class Router[F[_]: Effect](repo: CarRepo[F]) extends Http4sDsl[F] {
  import org.http4s.implicits._
  import cats.implicits._

  // AK - Nothing but assumptions here; this is just for demonstration
  val corsConfig = CORSConfig(
    anyOrigin = true,
    anyMethod = false,
    allowedMethods = Some(Set("GET", "POST")),
    allowCredentials = true,
    maxAge = 1.day.toSeconds
  )

  def api =
    CORS(
      Http4sRouter[F](
        "/api/1.0" -> cars
      ),
      corsConfig
    ).orNotFound

  // ---------------------------------------------------------------------------
  private val cars = HttpRoutes.of[F] {
    case GET -> Root / "cars" :? CarIdQueryParamMatcher(id) =>
      validateId(
        id,
        id =>
          repo
            .get(id)
            .value
            .flatMap(_.fold(err => NotFound(err), car => Ok(car.toDto)))
      )

    case GET -> Root / "cars" :? SortOrderQueryParamMatcher(sortOrder) =>
      validateParam[SortOrder](
        sortOrder.getOrElse(Some(SortOrder.ByCarId)),
        ord => repo.getAll(ord) >>= (cars => Ok(cars.map(_.toDto))),
        "Bad sort order"
      )

    case req @ POST -> Root / "cars" =>
      newId >>= (id => validateRequest(req, id, insertCar(repo)))

    case req @ PUT -> Root / "cars" :? CarIdQueryParamMatcher(id) =>
      validateId(id, id => validateRequest(req, id, updateCar(repo)))

    case DELETE -> Root / "cars" :? CarIdQueryParamMatcher(id) =>
      validateId(id, deleteCar(repo))

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

  private def insertCar(repo: CarRepo[F])(car: Car): F[Response[F]] =
    toEmptyResponse(repo.insert(car.id, car))

  private def updateCar(repo: CarRepo[F])(car: Car): F[Response[F]] =
    toEmptyResponse(repo.update(car.id, car))

  private def deleteCar(repo: CarRepo[F])(id: Car.Id): F[Response[F]] =
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

  private def validateParam[A](
      id: Option[A],
      success: A => F[Response[F]],
      bad: String
  ) =
    id.fold(BadRequest(bad))(success)

  private def validateId(
      id: Option[Car.Id],
      success: Car.Id => F[Response[F]]
  ) =
    validateParam(id, success, "Bad Id")

  private def newId = Effect[F].delay(Car.Id(UUID.randomUUID))

  // ---------------------------------------------------------------------------

  implicit def carIdQueryParamDecoder: QueryParamDecoder[Option[Car.Id]] =
    QueryParamDecoder[String].map(Car.Id.from)

  object CarIdQueryParamMatcher
      extends QueryParamDecoderMatcher[Option[Car.Id]]("carId")

  implicit def sortOrderQueryParamDecoder
      : QueryParamDecoder[Option[SortOrder]] =
    QueryParamDecoder[String].map(SortOrder.fromString)

  object SortOrderQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[Option[SortOrder]]("sortOrder")

}
