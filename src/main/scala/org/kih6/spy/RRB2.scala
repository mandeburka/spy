package org.kih6.spy

import com.pi4j.io.gpio._
import com.pi4j.wiringpi.{Gpio, SoftPwm}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

case class Speed(value: Int) {
  require(value >= 0 && value <= 100)
}

/**
 * Class to control RaspiRobot Board V2.
 * Rewritten from python original (see https://github.com/simonmonk/raspirobotboard2)
 * @param gpio
 */
class RRB2(private val gpio: GpioController) {

  import RRB2._

  Gpio.wiringPiSetupGpio()
  SoftPwm.softPwmCreate(LEFT_GO_PIN.getAddress, 0, 100)
  SoftPwm.softPwmCreate(RIGHT_GO_PIN.getAddress, 0, 100)

  val leftDirectionPin = gpio.provisionDigitalOutputPin(LEFT_DIR_PIN)
  val rightDirectionPin = gpio.provisionDigitalOutputPin(RIGHT_DIR_PIN)

  val led1Pin = gpio.provisionDigitalOutputPin(LED1_PIN)
  val led2Pin = gpio.provisionDigitalOutputPin(LED2_PIN)

  val oc1Pin = gpio.provisionDigitalOutputPin(OC1_PIN)
  val oc2Pin = gpio.provisionDigitalOutputPin(OC2_PIN)

  val sw1Pin = gpio.provisionDigitalInputPin(SW1_PIN)
  val sw2Pin = gpio.provisionDigitalInputPin(SW2_PIN)

  val triggerPin = gpio.provisionDigitalOutputPin(TRIGGER_PIN)
  val echoPin = gpio.provisionDigitalInputPin(ECHO_PIN)

  /**
   * Move robot forward with given speed
   * @param speed speed of motors
   */
  def forward(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, PinState.LOW, speed, FORWARD)
  }

  /**
   * Stop robot
   */
  def stop(): Unit = {
    setMotors(Speed(0), FORWARD, Speed(0), FORWARD)
  }

  /**
   * Move robot backward with given speed
   * @param speed speed of motors
   */
  def reverse(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, REVERSE, speed, REVERSE)
  }

  /**
   * Controls two robots motors
   * @param leftSpeed left motor speed
   * @param leftDir left motor rotation direction
   * @param rightSpeed right motor speed
   * @param rightDir right motor rotation direction
   */
  def setMotors(leftSpeed: Speed, leftDir: Direction, rightSpeed: Speed, rightDir: Direction): Unit = {
    SoftPwm.softPwmWrite(LEFT_GO_PIN.getAddress, leftSpeed.value)
    SoftPwm.softPwmWrite(RIGHT_GO_PIN.getAddress, rightSpeed.value)
    leftDirectionPin.setState(leftDir)
    rightDirectionPin.setState(rightDir)
  }

  /**
   * Turn left with given speed.
   * @param speed speed of motors
   */
  def left(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, FORWARD, speed, REVERSE)
  }

  /**
   * Turn right with given speed.
   * @param speed speed of motors
   */
  def right(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, REVERSE, speed, FORWARD)
  }

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


object RRB2 {
  type Direction = PinState
  final val LEFT_GO_PIN = RaspiBcmPin.GPIO_17
  final val LEFT_DIR_PIN = RaspiBcmPin.GPIO_04
  final val RIGHT_GO_PIN = RaspiBcmPin.GPIO_10
  final val RIGHT_DIR_PIN = RaspiBcmPin.GPIO_25
  final val SW1_PIN = RaspiBcmPin.GPIO_11
  final val SW2_PIN = RaspiBcmPin.GPIO_09
  final val LED1_PIN = RaspiBcmPin.GPIO_07
  final val LED2_PIN = RaspiBcmPin.GPIO_08
  final val OC1_PIN = RaspiBcmPin.GPIO_22
  final val OC2_PIN = RaspiBcmPin.GPIO_27
  final val TRIGGER_PIN = RaspiBcmPin.GPIO_18
  final val ECHO_PIN = RaspiBcmPin.GPIO_23
  final val FORWARD: Direction = PinState.LOW
  final val REVERSE: Direction = PinState.HIGH

  final val TRIGGER_TIME = 10.micros
  final val SOUND_SPEED = 340.29f;
  // speed of sound in m/s
  val NANOS_IN_SECOND = 1000000000

  def apply(): RRB2 = {
    GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING))
    val gpio: GpioController = GpioFactory.getInstance()
    new RRB2(gpio)
  }
}