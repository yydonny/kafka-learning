package yangyd.kf.processor.actor

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import yangyd.kf.commons.TransactionRecord
import yangyd.kf.processor.actor.RecordProcessorWorker.Batch

object RecordProcessorWorker {
  case class Batch(records: Seq[ConsumerRecord[String, TransactionRecord]])

  def props(partition: TopicPartition) = Props(classOf[RecordProcessorWorker], partition)
}

class RecordProcessorWorker(partition: TopicPartition) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Batch(records) ⇒
      log.info(" ( ͡° ͜ʖ ͡°) Processing {} messages from [{}/{}]", records.size, partition.topic(), partition.partition())
      for (record ← records) {
        log.info(" ( ͡° ͜ʖ ͡°) [{}] {}", record.offset(), record.value().getAmount)
      }
  }
}
