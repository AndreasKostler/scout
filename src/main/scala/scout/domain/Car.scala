package com.scout
package domain

case class Car(
    id: Car.Id,
    title: Car.Title,
    fuel: Fuel,
    price: Car.Price,
    condition: Condition
)

object Car {
  type Id = UuidString
  type Title = NonEmptyString
  type Price = PosDouble
}
