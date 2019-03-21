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

  def repoGen =
    for {
      cars <- Gen.listOfN(10, arbitrary[Car])
    } yield CarRepo.dummyInterpreter[IO](cars.map(c => (c.id, c)).toMap)

  val repo: CarRepo[IO] = repoGen.sample.get

}
