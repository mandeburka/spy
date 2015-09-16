package org.kih6.spy.rrb2

import com.pi4j.io.gpio.{PinState, GpioPinDigitalOutput, GpioPinDigitalInput}
import org.scalamock.scalatest.MockFactory
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.{Matchers, FlatSpec}

class DistanceSpec extends FlatSpec with MockFactory with Matchers {
  "Zero time signal" should "have zero distance" in {
    Distance.nanosToDistance(0) should be (0)
  }

  "2 second signal" should "be equal to sound speed divided by 1s" in {
    Distance.nanosToDistance(2.second.toNanos) should be (Distance.SOUND_SPEED)
  }

  "Ultrasonic distance calculation" should "use trigger and echo pins" in {
    val triggerPinMock = mock[GpioPinDigitalOutput]
    val echoPinMock = mock[GpioPinDigitalInput]

    val instance = new Distance {
      val triggerPin: GpioPinDigitalOutput = triggerPinMock
      val echoPin: GpioPinDigitalInput = echoPinMock
    }

    inSequence {
      (triggerPinMock.high _).expects()
      (triggerPinMock.low _).expects()
      (echoPinMock.getState _).expects().returns(PinState.HIGH)
      (echoPinMock.getState _).expects().returns(PinState.LOW)
    }
    Await.result(instance.distance, 1.second)
  }
}
