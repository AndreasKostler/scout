package com.scout
package repo

import cats.Monad
import cats.mtl.{FunctorRaise, MonadState}

import com.scout.domain.Car

trait CarRepo[F[_]] {

  // Insert a new car at `key`. Fails (AlreadyExists) if car already exists.
  def insert(key: Car.Id, entity: Car): F[Unit]

  // Get car at `key`. Fail (NotFound) if car does not exist.
  def get(key: Car.Id): F[Car]

  // Try get car at `key`.
  def find(key: Car.Id): F[Option[Car]]

  // Remove the car at `key`. Fails (NotFound) if car does not exist.
  def delete(id: Car.Id): F[Unit]

  /// Update the car at `key`. Fails (NotFound) if car does not exist. */
  def update(id: Car.Id, entity: Car): F[Unit]

}

object KeyValueOps {

  import cats.implicits._

  sealed trait Error extends Throwable

  case class NotFound(key: Car.Id) extends Error
  case class AlreadyExists(key: Car.Id) extends Error

  // --------------------------------------------------------------------------
  // For testing

  def pureInterpreter[F[_]: Monad](
      implicit
      state: MonadState[F, Map[Car.Id, Car]],
      err: FunctorRaise[F, Error]
  ) =
    new CarRepo[F] {

      def insert(key: Car.Id, entity: Car): F[Unit] =
        state.inspect(_.get(key)).flatMap {
          case Some(_) => err.raise(AlreadyExists(key))
          case None    => state.modify(_ + ((key, entity)))
        }

      def get(key: Car.Id): F[Car] =
        find(key).flatMap {
          case Some(a) => Monad[F].pure(a)
          case None    => err.raise(NotFound(key))
        }

      def find(key: Car.Id): F[Option[Car]] =
        state.inspect(_.get(key))

      def delete(key: Car.Id): F[Unit] =
        get(key) *> state.modify(_ - key)

      def update(key: Car.Id, entity: Car): F[Unit] =
        get(key) *> state.modify(_ + ((key, entity)))
    }
}
