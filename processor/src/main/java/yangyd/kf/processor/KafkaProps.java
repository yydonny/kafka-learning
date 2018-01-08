package yangyd.kf.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Properties;

@ConfigurationProperties(prefix = "kafka")
public class KafkaProps {
  private String topic;
  private String partitions; // comma separated list: '1,3,4,5'
  private final Properties consumer = new Properties();

  public int[] getPartitions() {
    try {
      return Arrays.stream(partitions.split(",")).mapToInt(Integer::parseInt).toArray();
    } catch (Exception e) {
      throw new RuntimeException("invalid config value for `kafka.partitions`: " + partitions);
    }
  }

  public void setPartitions(String partitions) {
    this.partitions = partitions;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public Properties getConsumer() {
    return consumer;
  }
}
