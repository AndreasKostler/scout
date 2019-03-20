package com

import eu.timepit.refined.api.Refined

import eu.timepit.refined.string._
import eu.timepit.refined.numeric._
import eu.timepit.refined.collection._

package object scout {
  type PosInt = Int Refined Positive
  type PosDouble = Double Refined Positive
  type UuidString = String Refined Uuid
  type NonEmptyString = String Refined NonEmpty
}
