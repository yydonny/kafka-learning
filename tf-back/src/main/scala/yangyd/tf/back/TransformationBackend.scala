package yangyd.tf.back

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, RootActorPath}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import com.typesafe.config.ConfigFactory
import yangyd.tf.back.Messages.{BackendRegistration, TransformationJob, TransformationResult}

class TransformationBackend extends Actor with ActorLogging {



  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive: Receive = {
    // the actual transformation work
    case TransformationJob(text) ⇒
      sender ! TransformationResult(text.toUpperCase)

    // subscribes to cluster events to detect new, potential, frontend nodes,
    // and send them a registration message so that they know that they can use the backend worker.
    case state: CurrentClusterState ⇒
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) ⇒ register(m)
  }

  def register(target: Member): Unit = {
    if (target.hasRole("frontend")) {
      val frontend = context.actorSelection(RootActorPath(target.address) / "user" / "frontend")
      log.info("=========== register to {}", frontend)
      frontend ! BackendRegistration
    }
  }



}

object TransformationBackend {
  def startup(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(
      s"""
        akka.remote.netty.tcp.port=$port
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [backend]
        """)
        .withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[TransformationBackend], name = "backend")
  }



}
