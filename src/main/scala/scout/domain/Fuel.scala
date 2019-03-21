package com.scout.domain

sealed trait Fuel

object Fuel {

  // AK - Add new fuel types here...
  case object Gasoline extends Fuel
  case object Diesel extends Fuel

  implicit val nonEmptyStringOrdering = new Ordering[Fuel] {
    def compare(x: Fuel, y: Fuel) =
      Ordering[String]
        .compare(x.getClass.getSimpleName, y.getClass.getSimpleName)
  }
}
