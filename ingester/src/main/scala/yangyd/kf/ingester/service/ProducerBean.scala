package yangyd.kf.ingester.service

import java.util.Properties
import java.util.concurrent.Future
import javax.annotation.PostConstruct

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import yangyd.kf.commons.{TransactionRecord, TransactionRecordSerializer}
import yangyd.kf.ingester.KafkaProps

import scala.collection.JavaConversions._

object ProducerBean {
  private val logger = LoggerFactory.getLogger(classOf[ProducerBean])
  private val KEY_SERIALIZER = "key.serializer"
  private val KEY_SERIALIZER_CLASS = classOf[StringSerializer].getCanonicalName
  private val VALUE_SERIALIZER = "value.serializer"
  private val VALUE_SERIALIZER_CLASS = classOf[TransactionRecordSerializer].getCanonicalName
}

@Service
@EnableConfigurationProperties(Array(classOf[KafkaProps]))
class ProducerBean @Autowired()(val props: KafkaProps) {
  import ProducerBean._

  private val topic: String = props.getTopic

  private var producer: KafkaProducer[String, TransactionRecord] = _

  @PostConstruct
  def init(): Unit = {
    val producerConfig = props.getProducer
    producerConfig.setProperty(KEY_SERIALIZER, KEY_SERIALIZER_CLASS)
    producerConfig.setProperty(VALUE_SERIALIZER, VALUE_SERIALIZER_CLASS)
    if (StringUtils.isEmpty(props.getTopic)) {
      throw new IllegalArgumentException("kafka.topic is not configured!")
    }
    setup(producerConfig)
  }

  private def setup(config: Properties): Unit = {
    producer = new KafkaProducer[String, TransactionRecord](config)
    logger.info("initializing producer for topit {}...", topic)
    try {
      val partitions = producer.partitionsFor(topic)
      for (p ← partitions) {
        logger.info(s"discovered partition [${p.topic()},${p.partition()}] on broker ${p.leader().id()}")
      }
    } catch {
      case e: Exception ⇒
        logger.error("Unable to communicate with brokers. Is the topic '{}' created on the brokers?", props.getTopic)
        throw e
    }
  }

  def send(transactionRecord: TransactionRecord): Future[RecordMetadata] = producer send new ProducerRecord[String, TransactionRecord](topic,
        String.valueOf(transactionRecord.getAcceptTime.toEpochMilli * 1000),
        transactionRecord)

}
