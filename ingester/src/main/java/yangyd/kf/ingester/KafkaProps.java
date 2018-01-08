package yangyd.kf.ingester;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@ConfigurationProperties(prefix = "kafka")
public class KafkaProps {
  private final Properties producer = new Properties();

  private String topic;

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public Properties getProducer() { // must be public
    return producer;
  }
}
