package com

import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string._
import eu.timepit.refined.numeric._
import eu.timepit.refined.collection._

package object scout {
  type PosInt = Int Refined Positive
  object PosInt extends RefinedTypeOps[PosInt, Int]
  implicit val posIntOrdering = new Ordering[PosInt] {
    def compare(x: PosInt, y: PosInt) =
      Ordering[Int].compare(x.value, y.value)
  }

  type PosDouble = Double Refined Positive
  object PosDouble extends RefinedTypeOps[PosDouble, Double]
  implicit val posDoubleOrderingouble = new Ordering[PosDouble] {
    def compare(x: PosDouble, y: PosDouble) =
      Ordering[Double].compare(x.value, y.value)
  }

  type UuidString = String Refined Uuid
  object UuidString extends RefinedTypeOps[UuidString, String]
  implicit val uuidStringOrdering = new Ordering[UuidString] {
    def compare(x: UuidString, y: UuidString) =
      Ordering[String].compare(x.value, y.value)
  }

  type NonEmptyString = String Refined NonEmpty
  object NonEmptyString extends RefinedTypeOps[NonEmptyString, String]
  implicit val nonEmptyStringOrdering = new Ordering[NonEmptyString] {
    def compare(x: NonEmptyString, y: NonEmptyString) =
      Ordering[String].compare(x.value, y.value)
  }

}
