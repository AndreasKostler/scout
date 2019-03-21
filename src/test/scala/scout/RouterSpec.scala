package com.scout

import org.scalatest._
import org.scalatest.prop._
import org.http4s.{Status => HttpStatus, _}
import org.scalacheck.Arbitrary._
import org.scalacheck._
import cats.effect.IO
import com.scout.domain._

// AK - These tests only test request validation; they're integration tests
// of sorts
class RouterSpec
    extends FlatSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with ArbitraryMother
    with Transport {

  val router = new Router(repo)

  val title = NonEmptyString.unsafeFrom("Foo")

  "The router" should "accept valid GET requests" in {
    forAll(validGetGen) { (req: Request[IO]) =>
      checkOk(fire(router, req))
    }
  }

  "The router" should "reject invalid GET requests with BAD-REQUEST" in {
    forAll(invalidGetGen) { (req: Request[IO]) =>
      checkBad(fire(router, req))
    }
  }

  "The router" should "accept valid POST requests" in {
    List(validNewCar(title), validNewCar(title)).foreach(
      req => checkOk(fire(router, post(toDto(req))))
    )
  }

  "The router" should "reject invalid POST requests with BAD-REQUEST" in {
    List(
      invalidateNewCarReq(toDto(validNewCar(title))),
      invalidateUsedCarReq(toDto(validUsedCar(title)))
    ).foreach(
      req => checkBad(fire(router, post(req)))
    )
  }

  "The router" should "accept valid PUT requests" in {
    List(
      validNewCar(title),
      validUsedCar(title)
    ).map(c => (c.id, toDto(c)))
      .foreach {
        case (id, req) => checkOk(fire(router, put(id, req)))
      }
  }

  "The router" should "reject invalid PUT requests with BAD-REQUEST" in {
    val newCar = validNewCar(title)
    val usedCar = validUsedCar(title)
    List(
      (newCar.id, invalidateNewCarReq(toDto(newCar))),
      (usedCar.id, invalidateUsedCarReq(toDto(usedCar)))
    ).foreach {
      case (id, req) => checkBad(fire(router, put(id, req)))
    }

  }

  "The router" should "accept valid DELETE requests" in {
    List(
      validNewCar(title),
      validUsedCar(title)
    ).foreach(req => checkOk(fire(router, delete(req.id))))
  }

  "The router" should "reject invalid DELETE requests with BAD-REQUEST" in {
    forAll { (id: String) =>
      checkBad(
        fire(
          router,
          Request[IO](
            method = Method.DELETE,
            uri = root.withQueryParam("carId", id)
          )
        )
      )
    }
  }

  // ---------------------------------------------------------------------------
  private def validGetGen =
    for {
      carId <- arbitrary[Car.Id]
      order <- sortOrderGen
      req <- Gen.oneOf(
        get(),
        get("carId" -> carId.value),
        get("sortOrder" -> order.toString)
      )
    } yield req

  private def invalidGetGen =
    for {
      badId <- Gen.alphaStr
      badSortOrder <- Gen.alphaStr
      req <- Gen.oneOf(get("carId" -> badId), get("sortOrder" -> badSortOrder))
    } yield req

  private def checkOk[A](actual: IO[Response[IO]]) = {
    val actualResp = actual.unsafeRunSync
    actualResp.status should not be (HttpStatus.BadRequest)
  }

  private def checkBad[A](actual: IO[Response[IO]]) = {
    val actualResp = actual.unsafeRunSync
    actualResp.status shouldBe (HttpStatus.BadRequest)
  }

}
