package org.kih6.spy.rrb2

import com.pi4j.io.gpio.{PinState, GpioPinDigitalOutput}

case class Speed(value: Int) {
  require(value >= 0 && value <= 100)
}

/**
 * Control RaspiRobot Board v2 motors
 */
trait Move {
  import Move._
  val leftGoPin: PwmPin
  val leftDirectionPin: GpioPinDigitalOutput
  val rightGoPin: PwmPin
  val rightDirectionPin: GpioPinDigitalOutput

  /**
   * Controls two robots motors
   * @param leftSpeed left motor speed
   * @param leftDir left motor rotation direction
   * @param rightSpeed right motor speed
   * @param rightDir right motor rotation direction
   */
  def setMotors(leftSpeed: Speed, leftDir: Direction, rightSpeed: Speed, rightDir: Direction): Unit = {
    leftGoPin.setValue(leftSpeed.value)
    leftDirectionPin.setState(leftDir)
    rightGoPin.setValue(rightSpeed.value)
    rightDirectionPin.setState(rightDir)
  }

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
}

object Move {
  type Direction = PinState

  final val FORWARD: Direction = PinState.LOW
  final val REVERSE: Direction = PinState.HIGH
}
