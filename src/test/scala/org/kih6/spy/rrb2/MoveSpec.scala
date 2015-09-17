package org.kih6.spy.rrb2

import com.pi4j.io.gpio.{PinState, GpioPinDigitalOutput}
import org.scalamock.MockParameter
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

class MoveSpec extends FlatSpec with MockFactory with Matchers {
  case class MockedMove(
    leftGoPinMock: PwmPin,
    leftDirectionPinMock: GpioPinDigitalOutput,
    rightGoPinMock: PwmPin,
    rightDirectionPinMock: GpioPinDigitalOutput,
    move: Move)

  /**
   * Mocks left and right motor pins
   * @return mocked pins and Move instance
   */
  def fixture: MockedMove = {
    val leftGoPinMock = mock[PwmPin]
    val leftDirectionPinMock = mock[GpioPinDigitalOutput]
    val rightGoPinMock = mock[PwmPin]
    val rightDirectionPinMock = mock[GpioPinDigitalOutput]
    MockedMove(
      leftGoPinMock,
      leftDirectionPinMock,
      rightGoPinMock,
      rightDirectionPinMock,
      new Move {
        override val leftGoPin: PwmPin = leftGoPinMock
        override val leftDirectionPin: GpioPinDigitalOutput = leftDirectionPinMock
        override val rightGoPin: PwmPin = rightGoPinMock
        override val rightDirectionPin: GpioPinDigitalOutput = rightDirectionPinMock
      }
    )
  }

  /**
   * Generalize expectation of pin state at specific move
   * @param mockedMove
   * @param lgp leftGoPin value expectation
   * @param ldp leftDirectionPin state expectation
   * @param rgp rightGoPin value expectation
   * @param rdp rightDirectionPin state expectation
   * @return
   */
  def addExpectations(
      mockedMove: MockedMove,
      lgp: MockParameter[Int],
      ldp: MockParameter[Move.Direction],
      rgp: MockParameter[Int],
      rdp: MockParameter[Move.Direction]): MockedMove = {

    (mockedMove.leftGoPinMock.setValue _).expects(lgp)
    (mockedMove.rightGoPinMock.setValue _).expects(rgp)
    (mockedMove.leftDirectionPinMock.setState(_:PinState)).expects(ldp)
    (mockedMove.rightDirectionPinMock.setState(_:PinState)).expects(rdp)
    mockedMove
  }

  "Move forward" should "turn both wheels with the same speed" in {
    val speed = Speed(20)
    addExpectations(fixture, speed.value, *, speed.value, *).move.forward(speed)
  }

  it should "turn both wheels in forward direction" in {
    addExpectations(fixture, *, Move.FORWARD, *, Move.FORWARD).move.forward()
  }

  "Move reverse" should "turn both wheels with the same speed" in {
    val speed = Speed(20)
    addExpectations(fixture, speed.value, *, speed.value, *).move.reverse(speed)
  }

  it should "turn both wheels in reverse direction" in {
    addExpectations(fixture, *, Move.REVERSE, *, Move.REVERSE).move.reverse()
  }

  "Move left" should "turn both wheels with the same speed" in {
    val speed = Speed(20)
    addExpectations(fixture, speed.value, *, speed.value, *).move.left(speed)
  }

  it should "turn right wheel in reverse direction" in {
    addExpectations(fixture, *, *, *, Move.REVERSE).move.left()
  }

  it should "turn left wheel in forward direction" in {
    addExpectations(fixture, *, Move.FORWARD, *, *).move.left()
  }

  "Move right" should "turn both wheels with the same speed" in {
    val speed = Speed(20)
    addExpectations(fixture, speed.value, *, speed.value, *).move.right(speed)

  }

  it should "turn right wheel in forward direction" in {
    addExpectations(fixture, *, *, *, Move.FORWARD).move.right()
  }

  it should "turn left wheel in reverse direction" in {
    addExpectations(fixture, *, Move.REVERSE, *, *).move.right()
  }
}
