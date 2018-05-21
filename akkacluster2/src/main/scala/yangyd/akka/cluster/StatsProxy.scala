package yangyd.akka.cluster

import akka.actor.{ActorSystem, Props}

object StatsProxy {
  def startup(): Unit = {
    // note that client is not a compute node, role not defined
    val system = ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[StatsClientActor], "/user/statsServiceProxy"), "client")
  }
}
