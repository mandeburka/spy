package org.kih6.spy

import com.pi4j.io.gpio._
import com.pi4j.wiringpi.{Gpio, SoftPwm}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Speed(value: Int) {
  require(value >= 0 && value <= 100)
}


class RRB2 (private val gpio: GpioController) {
  import RRB2._

  Gpio.wiringPiSetup()
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

  def setMotors(leftSpeed: Speed, leftDir: Direction, rightSpeed: Speed, rightDir: Direction): Unit = {
    SoftPwm.softPwmWrite(LEFT_GO_PIN.getAddress, leftSpeed.value)
    SoftPwm.softPwmWrite(RIGHT_GO_PIN.getAddress, rightSpeed.value)
    leftDirectionPin.setState(leftDir)
    rightDirectionPin.setState(rightDir)
  }

  def forward(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, PinState.LOW, speed, FORWARD)
  }

  def stop(): Unit = {
    setMotors(Speed(0), FORWARD, Speed(0), FORWARD)
  }

  def reverse(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, REVERSE, speed, REVERSE)
  }

  def left(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, FORWARD, speed, REVERSE)
  }

  def right(speed: Speed = Speed(50)): Unit = {
    setMotors(speed, REVERSE, speed, FORWARD)
  }

  def distance: Future[Double] = {
    Future {
      //    trigger sensor
      triggerPin.high()
      Thread.sleep(0, TRIGGER_TIME.toNanos.asInstanceOf[Int])
      triggerPin.low()

      //    wait for signal
      waitFor(echoPin, PinState.HIGH)
      val start = System.nanoTime()
      waitFor(echoPin, PinState.LOW)
      val end = System.nanoTime()

      val duration: Long = end - start
      duration * SOUND_SPEED / (2 * NANOS_IN_SECOND)
    }
  }

  @annotation.tailrec
  private def waitFor(pin: GpioPinDigital, state: PinState): Unit = {
    if (pin.getState != state) waitFor(pin, state)
  }
}


object RRB2 {
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

  type Direction = PinState
  final val FORWARD: Direction = PinState.LOW
  final val REVERSE: Direction = PinState.HIGH

  final val TRIGGER_TIME = 10.micros
  final val SOUND_SPEED = 340.29f;  // speed of sound in m/s
  val NANOS_IN_SECOND = 1000000000

  def apply(): RRB2 = {
    GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING))
    val gpio: GpioController = GpioFactory.getInstance()
    new RRB2(gpio)
  }
}