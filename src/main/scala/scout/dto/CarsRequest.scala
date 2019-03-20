package com.scout
package dto

import java.time.LocalDateTime
import com.scout.domain._

case class CarsRequest(
    id: Car.Id,
    title: Car.Title,
    fuel: Fuel,
    price: Car.Price,
    isNew: Boolean,
    mileage: Option[PosInt],
    firstRegistration: Option[LocalDateTime]
) {

  def toCar: Option[domain.Car] = {
    val condition: Option[Condition] =
      if (isNew) Some(Condition.New)
      else
        for {
          m <- mileage
          r <- firstRegistration
        } yield Condition.Used(m, r)

    condition.map(c => Car(id, title, fuel, price, c))
  }
}
