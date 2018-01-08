package yangyd.kf.ingester.service

import java.util.concurrent.{Callable, ScheduledExecutorService, TimeUnit}

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import yangyd.kf.commons.TransactionRecord
import yangyd.kf.ingester.model.KafkaMessageMetaData

import scala.language.implicitConversions

object RecordGenerator {
  val logger: Logger = LoggerFactory.getLogger(classOf[RecordGenerator])
  private val writer = new ObjectMapper().writerWithDefaultPrettyPrinter()
  private def json(o: Object) = writer.writeValueAsString(o)

  implicit def runnable(f: ⇒ Unit): Runnable = new Runnable() { def run(): Unit = f }
  implicit def callable[A <: Long](f: () ⇒ A): Callable[A] = new Callable[A] { override def call(): A = f() }
}

@Component
class RecordGenerator @Autowired()(executorService: ScheduledExecutorService,
                                   producer: ProducerBean) extends CommandLineRunner
{
  import RecordGenerator._

  def produce(): Unit = {
    val result = producer send TransactionRecord.random()
    logger.info(new KafkaMessageMetaData(result.get()).toString)
  }

  override def run(strings: String*): Unit = {
    logger.info("start producing...")
    executorService.scheduleAtFixedRate(produce(), 2, 2, TimeUnit.SECONDS)
  }
}
