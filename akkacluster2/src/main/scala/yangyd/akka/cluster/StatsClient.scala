package yangyd.akka.cluster

import akka.actor.{ActorSystem, Props}

object StatsClient {
  def startup(): Unit = {
    // client is not a compute node, role not defined
    val system = ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[StatsClientActor], "/user/statsService"), "client")
  }
}
