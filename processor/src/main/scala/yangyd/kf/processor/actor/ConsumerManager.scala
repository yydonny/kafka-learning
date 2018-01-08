package yangyd.kf.processor.actor

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import yangyd.kf.commons.TransactionRecord

import scala.concurrent.duration._
import scala.language.postfixOps

object ConsumerManager {

  case object Ready

  case object Poll

  case class PollError(e: Throwable)

  case class PollResult(consumerRecords: ConsumerRecords[String, TransactionRecord])

}

class ConsumerManager(partitions: Map[TopicPartition, Long]) extends Actor with ActorLogging {
  import ConsumerManager._
  private val poller = context.actorOf(RecordPoller.props(partitions), "poller")
  private val processor = context.actorOf(RecordProcessor.props, "processor")

  self ! Ready

  override def receive: Receive = {
    case Ready ⇒ poller ! Poll
    case r: PollResult ⇒ processor ! r
    case PollError(e) ⇒ throw e
  }
}
