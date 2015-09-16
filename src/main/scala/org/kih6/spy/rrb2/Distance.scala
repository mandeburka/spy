package org.kih6.spy.rrb2

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.pi4j.io.gpio.{GpioPinDigital, PinState, GpioPinDigitalInput, GpioPinDigitalOutput}

/**
 * Calculates distance to obstacle with
 * RaspiRobot Board v2 and Ultrasonic Ranging Module HC-SR04
 */
trait Distance {
  final val TRIGGER_TIME = 10.micros
  final val SOUND_SPEED = 340.29f
  final val NANOS_IN_SECOND = 1000000000

  val triggerPin: GpioPinDigitalOutput
  val echoPin: GpioPinDigitalInput

  /**
   * Get distance to obstacle
   * @return distance wrapped in future
   */
  def distance: Future[Double] = {
    for {
      _ <- triggerSignal
      _ <- waitFor(echoPin, PinState.HIGH)
      start = System.nanoTime()
      _ <- waitFor(echoPin, PinState.LOW)
      end = System.nanoTime()
    } yield nanosToDistance(end - start)
  }

  /**
   * Converts time spent by sound wave to go to obstacle and return into distance to obstacle
   * @param nanos time spent by sound wave to go to obstacle and return
   * @return distance to obstacle
   */
  private def nanosToDistance(nanos: Long): Double = {
    nanos * SOUND_SPEED / (2 * NANOS_IN_SECOND)
  }

  /**
   * Emits ultrasonic signal
   * @return Future which succeeds once signal is sent
   */
  private def triggerSignal: Future[Unit] = {
    Future {
      triggerPin.high()
      Thread.sleep(0, TRIGGER_TIME.toNanos.asInstanceOf[Int])
      triggerPin.low()
    }
  }

  /**
   * blocks till pin goes to given state
   * @param pin to watch
   * @param state to wait for
   * @return Future which succeeds once pin state is equal to given
   */
  private def waitFor(pin: GpioPinDigital, state: PinState): Future[Unit] = {
    Future {
      while (pin.getState != state) {
        Thread.sleep(0, 1)
      }
    }
  }
}
