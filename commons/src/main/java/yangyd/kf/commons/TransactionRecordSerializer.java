package yangyd.kf.commons;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class TransactionRecordSerializer implements Serializer<TransactionRecord> {
  private static final ObjectWriter json = new ObjectMapper().writer();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public byte[] serialize(String topic, TransactionRecord data) {
    try {
      return json.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
  }
}

