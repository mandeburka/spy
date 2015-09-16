package org.kih6.spy.rrb2

import scala.concurrent.duration._

import org.scalatest.{Matchers, FlatSpec}

class DistanceSpec extends FlatSpec with Matchers {
  "Zero time signal" should "have zero distance" in {
    Distance.nanosToDistance(0) should be (0)
  }

  "2 second signal" should "be equal to sound speed" in {
    Distance.nanosToDistance(2.second.toNanos) should be (Distance.SOUND_SPEED)
  }
}
