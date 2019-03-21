package com.scout
package dto

import java.time.LocalDateTime
import com.scout.domain._

case class CarResponse(
    id: Car.Id,
    title: Car.Title,
    fuel: Fuel,
    price: Car.Price,
    isNew: Boolean,
    mileage: Option[PosInt],
    firstRegistration: Option[LocalDateTime]
) {
  def toCar: Option[domain.Car] =
    Condition
      .from(isNew, mileage, firstRegistration)
      .map(c => Car(id, title, fuel, price, c))
}
