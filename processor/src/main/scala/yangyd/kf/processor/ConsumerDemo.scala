package yangyd.kf.processor

import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.common.errors.WakeupException
import org.springframework.beans.factory.annotation.Autowired
import resource._
import yangyd.kf.commons.TransactionRecord

import scala.collection.JavaConversions._

class ConsumerDemo @Autowired()(consumer: KafkaConsumer[String, TransactionRecord]) {
  def foo(): Unit = {
    for (consumer ← managed(consumer)) {
      consumer.subscribe(List("foo", "bar"))
      while (true) {
        val records: ConsumerRecords[String, TransactionRecord] = consumer.poll(100)
      }
    }
  }

  /**
    * Manually commit the offsets only after the corresponding records have been processed.
    * This is "at-least-once" delivery implementation, as each record will likely be delivered one time
    * but in failure cases could be duplicated.
    *
    * A better way is writing offsets to the target database as the records in a single transaction.
    */
  def bar(): Unit = for (consumer ← managed(consumer)) {
    consumer.subscribe(List("foo", "bar"))
    while (true) {
      val allRecords: ConsumerRecords[String, TransactionRecord] = consumer.poll(Long.MaxValue)
      for (partition ← allRecords.partitions()) {
        val partitionRecords = allRecords.records(partition)

        // process records
        for { r ← partitionRecords } println(r)

        // commit offsets
        val lastOffset = partitionRecords.get(partitionRecords.size - 1).offset
        // The committed offset must be the offset of the next message that your application will read.
        consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)))
      }
    }
  }

  /**
    * multi-threaded consumer control
    */
  class Runner(consumer: KafkaConsumer[String, TransactionRecord]) extends Runnable {
    val stop = new AtomicBoolean(false)

    private def closed = stop.get()

    // stop the consumer even though its blocked waiting for new messages.
    def shutdown(): Unit = {
      stop.set(true)
      consumer.wakeup()
    }

    override def run(): Unit = {
      for (c ← managed(consumer)) {
        c.subscribe(List("topic"))
        try {
          while (!closed) {
            c.poll(10000)
          }
        } catch {
          case e: WakeupException if !closed ⇒ throw new RuntimeException("consumption aborted")
          case _: Throwable ⇒ // ignore
        }
      }
    }
  }
}
