package yangyd.akka.cluster

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object StatsSample {
  def startup(configFile: String, ports: Array[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port when specified as program argument
      val config = ConfigFactory.parseString(
        s"""
        akka.remote.netty.tcp.port=$port
        akka.remote.artery.canonical.port=$port
        """)
          .withFallback(ConfigFactory.parseString("akka.cluster.roles = [compute]"))
          .withFallback(ConfigFactory.load(configFile))

      val system = ActorSystem("ClusterSystem", config)

      system.actorOf(Props[StatsWorker], name = "statsWorker")
      system.actorOf(Props[StatsService], name = "statsService")
    }
  }
}
