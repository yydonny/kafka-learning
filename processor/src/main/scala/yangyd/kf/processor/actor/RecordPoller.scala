package yangyd.kf.processor.actor

import java.util.concurrent.ExecutorService

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import yangyd.kf.commons.TransactionRecord
import yangyd.kf.processor.actor.ConsumerManager.{Poll, PollError, PollResult}
import yangyd.kf.processor.core.ApplicationContextSupport

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object RecordPoller {
  val POLL_TIMEOUT = 2000

  def props(partitions: Map[TopicPartition, Long]) = Props(classOf[RecordPoller], partitions)
}

/**
  * The actor that polls message from Kafka broker.
  *
  * @param partitions partitions and respective offsets to start consuming with. for offset, -1 means from beginning and -2 means from end.
  */
class RecordPoller(partitions: Map[TopicPartition, Long])
    extends Actor with ApplicationContextSupport with ActorLogging {

  import RecordPoller._

  private val consumer = applicationContext.getBean(classOf[KafkaConsumer[String, TransactionRecord]])
  private val executor = applicationContext.getBean(classOf[ExecutorService])
  private implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(executor)

  setup(partitions)

  /**
    * Post-Stop callback: Shutdown the consumer.
    */
  override def postStop(): Unit = {
    try {
      // If no thread is blocking in a method which can throw WakeupException,
      // the next call to such a method will raise it instead.
      while (true) consumer.wakeup()
    } catch {
      case _: WakeupException ⇒
        consumer.close()
      case t: Throwable ⇒
        log.error(t, "error stopping consumer")
    }
  }

  override def receive: Receive = {
    case Poll ⇒ Future {
      consumer.poll(POLL_TIMEOUT)
    } onComplete {
      case Success(crs) ⇒ context.parent ! PollResult(crs)
      case Failure(e) ⇒ e match {
        case we: WakeupException ⇒ log.warning("received wakeup() call, stop polling.")
        case _ ⇒ context.parent ! PollError(e)
      }
    }
  }

  private def setup(partitions: Map[TopicPartition, Long]): Unit = {
    log.info(s"assigning partitions ${partitions.keys}")
    consumer.assign(partitions.keys)
    for ((partition, offset) ← partitions) offset match {
      case -2 ⇒
        consumer.seekToEnd(Seq(partition))
        log.info("seeking to position [{}/end]", partition)
      case -1 ⇒
        consumer.seekToBeginning(Seq(partition))
        log.info("seeking to position [{}/beginning]", partition)
      case n if n > 0 ⇒
        consumer.seek(partition, n)
        log.info("seeking to position [{}/{}]", partition, n)
      case _ ⇒ throw new IllegalArgumentException("Invalid offset for partition " + partition)
    }
  }

}
