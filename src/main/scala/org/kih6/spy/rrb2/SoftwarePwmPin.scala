package org.kih6.spy.rrb2

import com.pi4j.wiringpi.SoftPwm

trait PwmPin {
  def setValue(value: Int)
}

/**
 * Simple wrapper around com.pi4j.wiringpi.SoftPwm class. See its javadoc for more details
 * @param pin The GPIO pin to use as a PWM pin.
 * @param value The value to initialize the PWM pin (between 0 (off) to 100 (fully on))
 * @param range The maximum range. Use 100 for the pwmRange.
 */
case class SoftwarePwmPin(pin: Int, value: Int = 0, range: Int = 100) extends PwmPin {
  SoftPwm.softPwmCreate(pin, value, range)

  def setValue(value: Int): Unit = {
    SoftPwm.softPwmWrite(pin, value)
  }
}
