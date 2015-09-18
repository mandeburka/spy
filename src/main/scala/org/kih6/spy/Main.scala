package org.kih6.spy

import akka.actor._
import org.kih6.spy.actors.SpyActor
import org.kih6.spy.rrb2.Speed

object Main {
  def main(args: Array[String]) {
    val system = ActorSystem("spiesSystem")
    val spy = system.actorOf(SpyActor.props, "spy")
    system.actorOf(Props(classOf[Terminator], spy), "terminator")

    spy ! SpyActor.Forward(Speed(50))
    spy ! SpyActor.Distance
    spy ! PoisonPill
  }
}

class Terminator(ref: ActorRef) extends Actor with ActorLogging {
  context watch ref
  def receive = {
    case Terminated(_) =>
      log.info("{} has terminated, shutting down system", ref.path)
      context.system.shutdown()
  }
}
