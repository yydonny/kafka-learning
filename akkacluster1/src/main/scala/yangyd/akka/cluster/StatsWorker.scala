package yangyd.akka.cluster

import akka.actor.Actor

class StatsWorker extends Actor {
  var cache = Map.empty[String, Int]
  def receive: Receive = {
    case word: String =>
      val length = cache.get(word) match {
        case Some(x) => x
        case None =>
          val x = word.length
          cache += (word -> x)
          x
      }
      sender() ! length
  }
}
