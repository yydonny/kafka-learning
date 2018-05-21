package yangyd.akka.cluster

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.typesafe.config.ConfigFactory

object StatsMaster {
  def startup(configFile: String, ports: Array[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port when specified as program argument
      val config =
        ConfigFactory.parseString(
          s"""
              akka.remote.netty.tcp.port=$port
              akka.remote.artery.canonical.port=$port
          """)
          .withFallback(ConfigFactory.parseString("akka.cluster.roles = [compute]"))
          .withFallback(ConfigFactory.load(configFile))

      val system = ActorSystem("ClusterSystem", config)

      system.actorOf(ClusterSingletonManager.props(
        singletonProps = Props[StatsService],
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system).withRole("compute")),
        name = "statsService")

      system.actorOf(ClusterSingletonProxy.props(singletonManagerPath = "/user/statsService",
        settings = ClusterSingletonProxySettings(system).withRole("compute")),
        name = "statsServiceProxy")
    }
  }
}
