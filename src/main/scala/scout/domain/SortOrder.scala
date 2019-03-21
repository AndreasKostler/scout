package com.scout

sealed trait SortOrder
object SortOrder {
  case object ByCarId extends SortOrder
  case object ByTitle extends SortOrder
  case object ByFuel extends SortOrder
  case object ByCondition extends SortOrder
  case object ByDateOfRegistration extends SortOrder
  case object ByMileage extends SortOrder

  def fromString(str: String): Option[SortOrder] = str.toLowerCase match {
    case "carid"        => Some(ByCarId)
    case "title"        => Some(ByTitle)
    case "fuel"         => Some(ByFuel)
    case "condition"    => Some(ByCondition)
    case "registration" => Some(ByDateOfRegistration)
    case "mileage"      => Some(ByMileage)
    case _              => None
  }

}
