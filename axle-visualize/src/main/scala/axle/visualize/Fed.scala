package axle.visualize

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import axle.quanta.Time
import axle.quanta.TimeConverter
import axle.quanta.UnittedQuantity

trait Fed[T] {

  def initialValue: T

  var dataFeedActorOpt: Option[ActorRef] = None

  def setFeeder(
    fn: T => T,
    interval: UnittedQuantity[Time, Double],
    system: ActorSystem)(
      implicit tc: TimeConverter[Double]): ActorRef = {
    val feederActorRef = system.actorOf(Props(new DataFeedActor(initialValue, fn, interval)))
    dataFeedActorOpt = Some(feederActorRef)
    feederActorRef
  }

  def feeder: Option[ActorRef] = dataFeedActorOpt

}
