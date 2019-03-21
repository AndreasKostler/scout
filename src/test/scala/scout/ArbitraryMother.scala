package com.scout

import cats.effect.IO
import java.time.{LocalDateTime, ZoneOffset}
import eu.timepit.refined.scalacheck.numeric._
import eu.timepit.refined.scalacheck.string._
import org.scalacheck.Arbitrary._
import org.scalacheck._
import org.scalacheck.ScalacheckShapeless._

import com.scout.repo.CarRepo
import com.scout.domain._
import com.scout.dto._

trait ArbitraryMother {

  implicit lazy val arbLocalDateTime: Arbitrary[LocalDateTime] = {
    import ZoneOffset.UTC
    Arbitrary {
      for {
        seconds <- Gen.chooseNum(
          LocalDateTime.MIN.toEpochSecond(UTC),
          LocalDateTime.MAX.toEpochSecond(UTC)
        )
        nanos <- Gen.chooseNum(
          LocalDateTime.MIN.getNano,
          LocalDateTime.MAX.getNano
        )
      } yield LocalDateTime.ofEpochSecond(seconds, nanos, UTC)
    }
  }

  implicit def arbCarId: Arbitrary[Car.Id] = Arbitrary {
    for {
      uuid <- Gen.uuid
    } yield Car.Id(uuid)
  }

  def carGen = arbitrary[Car]

  def repoGen =
    for {
      cars <- Gen.listOfN(10, arbitrary[Car])
    } yield CarRepo.dummyInterpreter[IO](cars.map(c => (c.id, c)).toMap)

  def sortOrderGen = Gen.oneOf("carid", "title", "fuel")

  val repo: CarRepo[IO] = repoGen.sample.get

  def validNewCar(title: Car.Title) =
    arbitrary[Car].sample.get
      .copy(condition = Condition.New, title = title)

  def validUsedCar(title: Car.Title) =
    arbitrary[Car].sample.get
      .copy(condition = arbitrary[Condition.Used].sample.get, title = title)

  def validNewCarReq(title: Car.Title) = toDto(validNewCar(title))

  def validUsedCarReq(title: Car.Title) = toDto(validUsedCar(title))

  def invalidateNewCarReq(req: CarRequest) =
    req.copy(
      mileage = Some(PosInt.unsafeFrom(1)),
      firstRegistration = Some(LocalDateTime.now)
    )

  def invalidateUsedCarReq(req: CarRequest) =
    req.copy(mileage = None, firstRegistration = None)

  // ---------------------------------------------------------------------------
  def toDto(car: Car) = {
    val (isNew, mileage, registration) = car.condition match {
      case Condition.New        => (true, None, None)
      case Condition.Used(m, r) => (false, Some(m), Some(r))
    }
    CarRequest(car.title, car.fuel, car.price, isNew, mileage, registration)
  }

}
