package yangyd.akka.cluster

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, Address, RelativeActorPath, RootActorPath}
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ClusterEvent._
import yangyd.kf.commons.StatsMessage.{JobFailed, StatsJob, StatsResult}

import scala.concurrent.duration._

class StatsClientActor(servicePath: String) extends Actor {
  val cluster = Cluster(context.system)
  private val servicePathElements = servicePath match {
    case RelativeActorPath(elements) => elements
    case _ => throw new IllegalArgumentException(
      "servicePath [%s] is not a valid relative actor path" format servicePath)
  }

  import context.dispatcher

  private val tickTask = context.system.scheduler.schedule(2.seconds, 2.seconds, self, "tick")

  var nodes = Set.empty[Address]

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[ReachabilityEvent])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    tickTask.cancel()
  }

  def receive: Receive = {
    case "tick" if nodes.nonEmpty =>
      // just pick any one
      val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)
      service ! StatsJob("this is the text that will be analyzed")
    case result: StatsResult =>
      println(result)
    case failed: JobFailed =>
      println(failed)
    case state: CurrentClusterState =>
      nodes = state.members.collect {
        case m if m.hasRole("compute") && m.status == MemberStatus.Up => m.address
      }

    case MemberUp(m) if m.hasRole("compute") =>
      nodes += m.address
    case other: MemberEvent =>
      nodes -= other.member.address
    case UnreachableMember(m) =>
      nodes -= m.address
    case ReachableMember(m) if m.hasRole("compute") =>
      nodes += m.address
  }

}
