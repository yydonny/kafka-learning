package yangyd.akka.cluster

import akka.actor.{Actor, ActorRef, ReceiveTimeout}
import yangyd.kf.commons.StatsMessage.{JobFailed, StatsResult}

import scala.concurrent.duration._

class StatsAggregator(expectedResults: Int, replyTo: ActorRef) extends Actor {
  private var results = IndexedSeq.empty[Int]
  context.setReceiveTimeout(3.seconds)

  def receive: Receive = {
    case wordCount: Int ⇒
      results = results :+ wordCount
      if (results.size == expectedResults) {
        val meanWordLength = results.sum.toDouble / results.size
        replyTo ! StatsResult(meanWordLength)
        context.stop(self)
      }

    case ReceiveTimeout =>
      replyTo ! JobFailed("Service unavailable, try again later")
      context.stop(self)
  }
}
