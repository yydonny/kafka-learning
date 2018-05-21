package yangyd.tf.back

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import yangyd.tf.back.Messages.{BackendRegistration, JobFailed, TransformationJob}
import concurrent.duration._
import akka.pattern.ask

class TransformationFrontend extends Actor {
  private var backends = IndexedSeq.empty[ActorRef]
  private var jobCounter = 0

  def receive: Receive = {
    case job: TransformationJob if backends.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later", job)

    // round-robin
    case job: TransformationJob ⇒
      jobCounter += 1
      backends(jobCounter % backends.size) forward job

    case BackendRegistration if !backends.contains(sender()) ⇒
      context watch sender()
      backends = backends :+ sender()

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}

object TransformationFrontend {
  def startup(args: Array[String]): Unit = {
    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"""
        akka.remote.netty.tcp.port=$port
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [frontend]
        """)
        .withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[TransformationFrontend], name = "frontend")

    val counter = new AtomicInteger
    import system.dispatcher
    system.scheduler.schedule(2.seconds, 2.seconds) {
      implicit val timeout: Timeout = Timeout(5.seconds)
      (frontend ? TransformationJob("hello-" + counter.incrementAndGet())) onSuccess {
        case result => println(result)
      }
    }
  }
}

