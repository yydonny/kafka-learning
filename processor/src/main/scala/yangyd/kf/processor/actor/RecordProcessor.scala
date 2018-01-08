package yangyd.kf.processor.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.kafka.common.TopicPartition
import yangyd.kf.processor.actor.ConsumerManager.{PollResult, Ready}

import scala.collection.JavaConversions._
import scala.concurrent.duration._


object RecordProcessor {
  def props = Props(classOf[RecordProcessor])
}

class RecordProcessor extends Actor with ActorLogging {
  import context.dispatcher
  private val scheduler = context.system.scheduler
  private var workers: Map[TopicPartition, ActorRef] = Map.empty

  private def worker(partition: TopicPartition) = workers.get(partition) match {
    case Some(worker) ⇒ worker
    case None ⇒
      val worker = context.actorOf(RecordProcessorWorker.props(partition), s"worker-${partition.partition()}")
      workers += partition → worker
      worker
  }

  override def receive: Receive = {
    case PollResult(crs) ⇒
      log.info(s"received a batch of ${crs.count()} records.")
      for (partition ← crs.partitions()) {
        worker(partition) ! RecordProcessorWorker.Batch(crs.records(partition))
      }
      next(crs.count())
  }

  private var backoff = 2

  /**
    * quadratic backoff if no records is processed this time, maximum 10 seconds
    */
  private def next(count: Int): Unit = {
    if (count == 0) {
      scheduler.scheduleOnce(backoff.seconds, context.parent, Ready)
      backoff *= 2
      if (backoff > 10) {
        backoff = 10
      }
    } else {
      context.parent ! Ready
      backoff = 2
    }
  }
}
