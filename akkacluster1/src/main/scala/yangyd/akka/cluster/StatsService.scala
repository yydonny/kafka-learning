package yangyd.akka.cluster

import akka.actor.{Actor, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.FromConfig
import yangyd.kf.commons.StatsMessage.StatsJob

class StatsService extends Actor {
  // This router is used both with lookup and deploy of routees. If you
  // have a router with only lookup of routees you can use Props.empty
  // instead of Props[StatsWorker.class].
  private val workerRouter = context.actorOf(FromConfig.props(Props[StatsWorker]), name = "workerRouter")

  def receive: Receive = {
    case StatsJob(text) if text != "" ⇒
      val words = text.split(" ")
      val replyTo = sender()
      // create actor that collects replies from workers
      val aggregator = context.actorOf(Props(classOf[StatsAggregator], words.size, replyTo))
      words foreach { word ⇒
        workerRouter.tell(ConsistentHashableEnvelope(word, word), aggregator)
      }
  }
}
w
