package com.scout.domain

sealed trait Fuel

object Fuel {

  // AK - Add new fuel types here...
  case object Gasoline extends Fuel
  case object Diesel extends Fuel
}
