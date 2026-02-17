package com.zeotap.fanout.transform;

import com.zeotap.fanout.model.Record;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class AvroTransformer implements Transformer {

    private final Schema schema;

    public AvroTransformer() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("record.avsc")) {
            if (is == null) {
                // Fallback if not found in classpath (during development)
                // In a real scenario, we might read from file or embed string
                String schemaJson = "{\"type\":\"record\",\"name\":\"AvroRecord\",\"namespace\":\"com.zeotap.fanout.avro\",\"fields\":[{\"name\":\"data\",\"type\":{\"type\":\"map\",\"values\":\"string\"}}]}";
                this.schema = new Schema.Parser().parse(schemaJson);
            } else {
                this.schema = new Schema.Parser().parse(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Avro schema", e);
        }
    }

    @Override
    public Object transform(Record record) {
        GenericRecord avroRecord = new GenericData.Record(schema);
        // Avro map values must be same type, assuming string for simplicity as per
        // schema
        // We need to convert Object values to String
        java.util.Map<String, String> stringUrlMap = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : record.data().entrySet()) {
            stringUrlMap.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        avroRecord.put("data", stringUrlMap);
        return avroRecord;
    }
}
