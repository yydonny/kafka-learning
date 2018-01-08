package yangyd.kf.ingester.model;

import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaMessageMetaData {
  private final long offset;
  private final long timestamp;
  private final int serializedKeySize;
  private final int serializedValueSize;
  private final int partition;
  private final String topic;

  public KafkaMessageMetaData(RecordMetadata rmd) {
    offset = rmd.offset();
    timestamp = rmd.timestamp();
    topic = rmd.topic();
    partition = rmd.partition();
    serializedKeySize = rmd.serializedKeySize();
    serializedValueSize = rmd.serializedValueSize();
  }

  public long getOffset() {
    return offset;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getSerializedKeySize() {
    return serializedKeySize;
  }

  public int getSerializedValueSize() {
    return serializedValueSize;
  }

  public int getPartition() {
    return partition;
  }

  public String getTopic() {
    return topic;
  }

  @Override
  public String toString() {
    return String.format("Committed record [%s,%d]-%d", topic, partition, offset);
  }
}
