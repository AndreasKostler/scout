package com.scout
package domain

import java.time.LocalDateTime

sealed trait Condition

object Condition {
  // AK - In the spec this is expressed by a boolean flag; this is is not safe
  // and not extensible. Condition does not need to be binary, e.g. the car could
  // have been written off or is sold for parts - both is quite common in Australia
  // for instance.
  case object New extends Condition
  case class Used(mileage: Used.Mileage, firstRegistration: LocalDateTime)
      extends Condition

  object Used {
    type Mileage = PosInt
  }
}
