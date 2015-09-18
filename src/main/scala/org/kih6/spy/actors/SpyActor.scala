package org.kih6.spy.actors

import akka.actor.{Props, Actor}
import org.kih6.spy.rrb2.Speed

class SpyActor extends Actor {
  import SpyActor._

  def receive = {
    case Forward(speed) => println(s"Going forward with speed ${speed.value}")
    case Reverse(speed) => println(s"Going backward with speed ${speed.value}")
    case Left(speed) => println(s"Turning left with speed ${speed.value}")
    case Right(speed) => println(s"Turning right forward with speed ${speed.value}")
    case Distance => println("Going to calculate distance")
  }
}

object SpyActor {
  sealed trait Message
  case class Forward(speed: Speed) extends Message
  case class Reverse(speed: Speed) extends Message
  case class Left(speed: Speed) extends Message
  case class Right(speed: Speed) extends Message
  case object Distance extends Message

  def props: Props = {
    Props(new SpyActor)
  }
}
