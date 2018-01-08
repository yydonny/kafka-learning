package yangyd.kf.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class TransactionRecordDeserializer implements Deserializer<TransactionRecord> {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public TransactionRecord deserialize(String topic, byte[] data) {
    try {
      return MAPPER.readValue(data, TransactionRecord.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
  }
}
