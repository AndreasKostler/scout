package scout

import org.scalatest._

class ScoutSpec extends FeatureSpec with GivenWhenThen {

  feature("The user can CRUD car listings") {

    info("As a user")
    info("I want to be able create, list, update, and delete car adverts")

    scenario("return the list of all cars") {

      When("no sort field is specified")
      assert(false)

      Then("the list should be sorted by id")
      assert(false)

      When("a sort field is specified")
      assert(false)

      Then("the list should be sorted by this field")
      assert(false)

    }

    scenario("return a car by id") {

      Given("the car for this id exists")
      assert(false)

      When("get is invoked for this id")
      assert(false)

      Then("the relevant car should be returned")
      assert(false)

      Given("the car for this id doesn't exist")
      assert(false)

      Then("the service should return NOT-FOUND")
      assert(false)
    }

    scenario("update a car by id") {

      Given("the car for this id exists")
      assert(false)

      When("put is invoked for this id")
      assert(false)

      Then("the relevant car should be updated")
      assert(false)

      Given("the car for this id doesn't exist")
      assert(false)

      Then("the service should return NOT-FOUND")
      assert(false)
    }

    scenario("delete a car by id") {

      Given("the car for this id exists")
      assert(false)

      When("delete is invoked for this id")
      assert(false)

      Then("the relevant car should be deleted")
      assert(false)

      Given("the car for this id doesn't exist")
      assert(false)

      Then("the service should return NOT-FOUND")
      assert(false)
    }

    scenario("create a new car advert") {

      When("post is invoked")
      assert(false)

      Then("then a new ad should be created")
      assert(false)
    }
  }
}
