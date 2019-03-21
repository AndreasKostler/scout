package com.scout
package repo

import cats.Applicative
import cats.data.{EitherT, OptionT}
import scala.collection.mutable

import com.scout.domain.Car

trait CarRepo[F[_]] {

  // Insert a new car at `key`. Fails (AlreadyExists) if car already exists.
  def insert(key: Car.Id, entity: Car): EitherT[F, CarRepo.Error, Unit]

  // Get all cars at `key`. Ordered by the given `order`
  def getAll(order: SortOrder): F[List[Car]]

  // Get car at `key`. Fails (NotFound) if car does not exist.
  def get(key: Car.Id): EitherT[F, CarRepo.Error, Car]

  // Try get car at `key`.
  def find(key: Car.Id): OptionT[F, Car]

  // Remove the car at `key`. Fails (NotFound) if car does not exist.
  def delete(id: Car.Id): EitherT[F, CarRepo.Error, Unit]

  // Update the car at `key`. Fails (NotFound) if car does not exist.
  def update(id: Car.Id, entity: Car): EitherT[F, CarRepo.Error, Unit]

}

object CarRepo {

  //import cats.implicits._

  sealed trait Error extends Throwable

  case class NotFound(key: Car.Id) extends Error
  case class AlreadyExists(key: Car.Id) extends Error

  // --------------------------------------------------------------------------
  // For testing

  def dummyInterpreter[F[_]: Applicative](init: Map[Car.Id, Car]) =
    new CarRepo[F] {

      val store = mutable.Map(init.toSeq: _*)

      def insert(key: Car.Id, entity: Car): EitherT[F, Error, Unit] =
        store.get(key) match {
          case Some(_) => EitherT.fromEither[F](Left(AlreadyExists(key)))
          case None => {
            store += (key -> entity)
            EitherT.rightT(())
          }
        }

      def get(key: Car.Id): EitherT[F, Error, Car] =
        EitherT(
          find(key).fold[Either[Error, Car]](Left(NotFound(key)))(x => Right(x))
        )

      def getAll(order: SortOrder): F[List[Car]] = {
        import SortOrder._
        val values = store.values.toList
        val ordered = order match {
          case ByCarId => values.sortBy(_.id)
          case ByTitle => values.sortBy(_.title)
          case ByFuel  => values.sortBy(_.fuel)
          //case ByCondition  => values.sortBy(_.condition)
          case _ => values.sortBy(_.id)
          // case ByDateOfRegistration  => value.sortBy(_.id)
          // case ByMileage  => value.sortBy(_.id)
        }

        Applicative[F].pure(ordered)
      }

      def find(key: Car.Id): OptionT[F, Car] =
        OptionT.fromOption(store.get(key))

      def delete(key: Car.Id): EitherT[F, Error, Unit] =
        get(key).map(_ => store -= key)

      def update(key: Car.Id, entity: Car): EitherT[F, Error, Unit] =
        get(key).map(_ => store += (key -> entity))
    }
}
