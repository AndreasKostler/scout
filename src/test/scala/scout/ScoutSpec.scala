package com.scout

import cats.effect.IO
import org.scalatest._
import org.scalatest.prop._

import eu.timepit.refined.scalacheck.numeric._
import eu.timepit.refined.scalacheck.string._
import org.scalacheck.ScalacheckShapeless._

import scala.util.Random
import org.scalacheck._
import org.scalacheck.Arbitrary._

import org.http4s.{Status => HttpStatus, _}
import org.http4s.circe.CirceEntityDecoder._
import io.circe.generic.auto._
import io.circe.refined._

import com.scout.domain._
import com.scout.repo.CarRepo
import com.scout.dto._

import java.util.UUID
import java.time.LocalDateTime

class ScoutSpec
    extends FeatureSpec
    with GivenWhenThen
    with GeneratorDrivenPropertyChecks
    with Matchers
    with ArbitraryMother
    with Transport {

  feature("The user can CRUD car listings") {
    info("As a user")
    info("I want to be able create, list, update, and delete car adverts")

    val router = new Router(repo)

    scenario("return the list of all cars") {
      When("no sort field is specified")
      Then("the list should be sorted by id")
      repo.getAll(SortOrder.ByCarId).unsafeRunSync
      check[List[CarResponse]](
        fire(router, get()),
        HttpStatus.Ok,
        Some(
          _.map(_.toCar.get) should contain theSameElementsInOrderAs (getAll(
            repo,
            None
          ))
        )
      )

      When("a sort field is specified")
      Then("the list should be sorted by this field")
      forAll(Gen.oneOf("carid", "title", "fuel")) { (so: String) =>
        val exp = getAll(repo, SortOrder.fromString(so))
        check[List[CarResponse]](
          fire(router, get("sortOrder" -> so)),
          HttpStatus.Ok,
          Some(_.map(_.toCar.get) should contain theSameElementsInOrderAs (exp))
        )
      }
    }

    scenario("return a car by id") {

      Given("the car for this id exists")
      val randomCar = repo
        .getAll(SortOrder.ByCarId)
        .map(cars => Random.shuffle(cars).head)
        .unsafeRunSync

      When("get is invoked for this id")
      Then("the relevant car should be returned")
      check[CarResponse](
        fire(router, get("carId" -> randomCar.id.value)),
        HttpStatus.Ok,
        Some(_.toCar.get shouldBe randomCar)
      )

      Given("the car for this id doesn't exist")
      val invalidId = Car.Id(UUID.randomUUID)
      When("get is invoked for this id")
      Then("the service should return NOT-FOUND")
      check[CarRepo.Error](
        fire(router, get("carId" -> invalidId.value)),
        HttpStatus.NotFound,
        Some(_ shouldBe CarRepo.NotFound(invalidId))
      )
    }

    scenario("update a car by id") {

      Given("the car for this id exists")
      val randomCar = repo
        .getAll(SortOrder.ByCarId)
        .map(cars => Random.shuffle(cars).head)
        .unsafeRunSync

      val modified =
        randomCar.copy(title = NonEmptyString.unsafeFrom("MyRandomCar"))

      When("put is invoked for this id")
      check[Unit](
        fire(
          router,
          put(modified.id, toDto(modified))
        ),
        HttpStatus.Ok,
        None
      )
      Then("the relevant car should be updated")
      check[CarResponse](
        fire(
          router,
          get("carId" -> modified.id.value)
        ),
        HttpStatus.Ok,
        Some(_.toCar.get shouldBe modified)
      )

      Given("the car for this id doesn't exist")
      Then("the service should return NOT-FOUND")
      val invalidCar =
        randomCar.copy(id = UuidString.unsafeFrom(UUID.randomUUID.toString))
      check[CarRepo.Error](
        fire(
          router,
          put(invalidCar.id, toDto(invalidCar))
        ),
        HttpStatus.NotFound,
        Some(_ shouldBe CarRepo.NotFound(invalidCar.id))
      )
    }

    scenario("delete a car by id") {

      Given("the car for this id exists")
      val randomCar = repo
        .getAll(SortOrder.ByCarId)
        .map(cars => Random.shuffle(cars).head)
        .unsafeRunSync
      check[CarResponse](
        fire(
          router,
          get("carId" -> randomCar.id.value)
        ),
        HttpStatus.Ok,
        Some(_.toCar.get shouldBe randomCar)
      )

      When("delete is invoked for this id")
      check[Unit](
        fire(
          router,
          delete(randomCar.id)
        ),
        HttpStatus.Ok,
        None
      )
      Then("the relevant car should be deleted")
      check[CarRepo.Error](
        fire(
          router,
          get("carId" -> randomCar.id.value)
        ),
        HttpStatus.NotFound,
        Some(_ shouldBe CarRepo.NotFound(randomCar.id))
      )

      Given("the car for this id doesn't exist")
      Then("the service should return NOT-FOUND")
      val invalidId = UuidString.unsafeFrom(UUID.randomUUID.toString)
      check[CarRepo.Error](
        fire(
          router,
          get("carId" -> invalidId.value)
        ),
        HttpStatus.NotFound,
        Some(_ shouldBe CarRepo.NotFound(invalidId))
      )
    }

    scenario("create a new car advert") {
      When("post is invoked with a new `new car`")
      val newTitle = NonEmptyString.unsafeFrom("MyNewCarCustomTitle")
      val newCarReq = validNewCarReq(newTitle)
      check[Unit](
        fire(
          router,
          post(newCarReq)
        ),
        HttpStatus.Ok,
        None
      )

      Given("the request validates")
      Then("a new ad should be created")
      check[List[CarResponse]](
        fire(
          router,
          get()
        ),
        HttpStatus.Ok,
        Some(_.filter(_.title == newTitle) shouldNot be(empty))
      )

      Given("the request doesn't validate")
      Then("the service should return BAD-REQUEST")
      val ivNewTitle = NonEmptyString.unsafeFrom("MyInvalidNewCarCustomTitle")
      val ivNewCarReq = invalidNewCarReq(ivNewTitle)
      check[Unit](
        fire(
          router,
          post(ivNewCarReq)
        ),
        HttpStatus.BadRequest,
        None
      )
      Then("no ad should be created")
      check[List[CarResponse]](
        fire(
          router,
          get()
        ),
        HttpStatus.Ok,
        Some(_.filter(_.title == ivNewTitle) should be(empty))
      )

      When("post is invoked with a new `used car`")
      val usedTitle = NonEmptyString.unsafeFrom("MyUsedCarCustomTitle")
      val usedCarReq = validUsedCarReq(usedTitle)
      check[Unit](
        fire(
          router,
          post(usedCarReq)
        ),
        HttpStatus.Ok,
        None
      )

      Given("the request validates")
      Then("a new ad should be created")
      check[List[CarResponse]](
        fire(
          router,
          get()
        ),
        HttpStatus.Ok,
        Some(_.filter(_.title == usedTitle) shouldNot be(empty))
      )

      Given("the request doesn't validate")
      Then("the service should return BAD-REQUEST")
      val ivUsedTitle = NonEmptyString.unsafeFrom("MyInvalidUsedCarCustomTitle")
      val ivUsedCarReq = invalidUsedCarReq(ivNewTitle)
      check[Unit](
        fire(
          router,
          post(ivUsedCarReq)
        ),
        HttpStatus.BadRequest,
        None
      )
      Then("no ad should be created")
      check[List[CarResponse]](
        fire(
          router,
          get()
        ),
        HttpStatus.Ok,
        Some(_.filter(_.title == ivUsedTitle) should be(empty))
      )
    }
  }

  // ---------------------------------------------------------------------------
  private def check[A](
      actual: IO[Response[IO]],
      expectedStatus: HttpStatus,
      bodyAssertion: Option[A => Assertion]
  )(
      implicit ev: EntityDecoder[IO, A]
  ) = {
    val actualResp = actual.unsafeRunSync
    val bodyCheck = bodyAssertion.fold[Assertion](
      actualResp.body.compile.toVector.unsafeRunSync shouldBe empty
    )(asst => asst(actualResp.as[A].unsafeRunSync))

    actualResp.status shouldBe expectedStatus
    bodyCheck
  }

  private def getAll(repo: CarRepo[IO], order: Option[SortOrder]) =
    repo.getAll(order.getOrElse(SortOrder.ByCarId)).unsafeRunSync

  private def toDto(car: Car) = {
    val (isNew, mileage, registration) = car.condition match {
      case Condition.New        => (true, None, None)
      case Condition.Used(m, r) => (false, Some(m), Some(r))
    }
    CarRequest(car.title, car.fuel, car.price, isNew, mileage, registration)
  }

  private def validNewCarReq(title: Car.Title) =
    toDto(
      arbitrary[Car].sample.get
        .copy(condition = Condition.New, title = title)
    )
  private def validUsedCarReq(title: Car.Title) =
    toDto(
      arbitrary[Car].sample.get
        .copy(condition = arbitrary[Condition.Used].sample.get, title = title)
    )

  private def invalidNewCarReq(title: Car.Title) =
    toDto(
      arbitrary[Car].sample.get
        .copy(condition = Condition.New, title = title)
    ).copy(
      mileage = Some(PosInt.unsafeFrom(1)),
      firstRegistration = Some(LocalDateTime.now)
    )

  private def invalidUsedCarReq(title: Car.Title) =
    toDto(
      arbitrary[Car].sample.get
        .copy(condition = arbitrary[Condition.Used].sample.get, title = title)
    ).copy(mileage = None, firstRegistration = None)

}
