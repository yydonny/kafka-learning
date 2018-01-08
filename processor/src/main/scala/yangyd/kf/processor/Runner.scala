package yangyd.kf.processor

import akka.actor.Props
import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import yangyd.kf.processor.actor.ConsumerManager
import yangyd.kf.processor.core.ActorSystemBean

@Service
@EnableConfigurationProperties(Array(classOf[KafkaProps]))
class Runner @Autowired()(props: KafkaProps,
                          applicationContext: ApplicationContext,
                          actorSystemBean: ActorSystemBean) extends CommandLineRunner {

  override def run(args: String*): Unit = {
    val manager = actorSystemBean.createActor(Props(classOf[ConsumerManager], initialOffsets), "kf-manager")
  }

  private def initialOffsets = props.getPartitions map {
    new TopicPartition(props.getTopic, _) → -1L
  } groupBy (_._1) map {
    case (k, v: Array[(TopicPartition, Long)]) ⇒ (k, v.map(_._2).head)
  }

}
