package org.kih6.spy.rrb2

import com.pi4j.io.gpio._
import com.pi4j.wiringpi.Gpio

/**
 * Class to control RaspiRobot Board V2.
 * Rewritten from python original (see https://github.com/simonmonk/raspirobotboard2)
 * @param gpio gpio controller to use
 */
class RRB2(private val gpio: GpioController) extends Robot {

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

  val leftGoPin = SoftwarePwmPin(LEFT_GO_PIN.getAddress)
  val leftDirectionPin = gpio.provisionDigitalOutputPin(LEFT_DIR_PIN)
  val rightGoPin = SoftwarePwmPin(RIGHT_GO_PIN.getAddress)
  val rightDirectionPin = gpio.provisionDigitalOutputPin(RIGHT_DIR_PIN)

  val led1Pin = gpio.provisionDigitalOutputPin(LED1_PIN)
  val led2Pin = gpio.provisionDigitalOutputPin(LED2_PIN)

  val oc1Pin = gpio.provisionDigitalOutputPin(OC1_PIN)
  val oc2Pin = gpio.provisionDigitalOutputPin(OC2_PIN)

  val sw1Pin = gpio.provisionDigitalInputPin(SW1_PIN)
  val sw2Pin = gpio.provisionDigitalInputPin(SW2_PIN)

  val triggerPin = gpio.provisionDigitalOutputPin(TRIGGER_PIN)
  val echoPin = gpio.provisionDigitalInputPin(ECHO_PIN)

}


object RRB2 {
  def apply(): RRB2 = {
    GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING))
    Gpio.wiringPiSetupGpio()
    new RRB2(GpioFactory.getInstance())
  }
}
