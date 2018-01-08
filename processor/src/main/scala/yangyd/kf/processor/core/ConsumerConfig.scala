package yangyd.kf.processor.core

import java.util.Properties
import java.util.concurrent.{ExecutorService, Executors}
import javax.annotation.PostConstruct

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, Configuration, Scope}
import yangyd.kf.commons.{TransactionRecord, TransactionRecordDeserializer}
import yangyd.kf.processor.KafkaProps

@Configuration
@EnableConfigurationProperties(Array(classOf[KafkaProps]))
class ConsumerConfig @Autowired()(props: KafkaProps) {
  private val config = new Properties()

  @PostConstruct
  def init(): Unit = {
    config.putAll(props.getConsumer)
    config.setProperty("key.deserializer", classOf[StringDeserializer].getCanonicalName)
    config.setProperty("value.deserializer", classOf[TransactionRecordDeserializer].getCanonicalName)
  }

  @Bean
  def consumerPool: ExecutorService = Executors.newFixedThreadPool(props.getPartitions.length)

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  def consumer(): KafkaConsumer[String, TransactionRecord] = {
    System.err.println("creating kafka consumer")
    config.list(System.err)
    new KafkaConsumer[String, TransactionRecord](config)
  }

  class ConsumerRunner() extends Runnable {
    override def run(): Unit = {
    }
  }

}
