package com.scout
package domain

import java.util.UUID

import com.scout.dto.CarResponse

case class Car(
    id: Car.Id,
    title: Car.Title,
    fuel: Fuel,
    price: Car.Price,
    condition: Condition
) {
  def toDto = {
    val (isNew, mileage, registration) = condition match {
      case Condition.New        => (true, None, None)
      case Condition.Used(m, r) => (false, Some(m), Some(r))
    }
    CarResponse(id, title, fuel, price, isNew, mileage, registration)
  }
}

object Car {
  type Id = UuidString
  object Id {
    def unsafeFrom(id: String): Id = UuidString.unsafeFrom(id)
    def from(id: String): Option[Id] = UuidString.from(id).toOption
    def apply(uuid: UUID): Id = UuidString.unsafeFrom(uuid.toString)
  }
  type Title = NonEmptyString
  type Price = PosDouble
}
