package com.scout
package dto

import java.time.LocalDateTime
import com.scout.domain._

case class CarRequest(
    title: Car.Title,
    fuel: Fuel,
    price: Car.Price,
    isNew: Boolean,
    mileage: Option[PosInt],
    firstRegistration: Option[LocalDateTime]
) {

  def toCar(id: Car.Id): Option[domain.Car] =
    Condition.from(isNew, mileage, firstRegistration).map(c => Car(id, title, fuel, price, c))

}
