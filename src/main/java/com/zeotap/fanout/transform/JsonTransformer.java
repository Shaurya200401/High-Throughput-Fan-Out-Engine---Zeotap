package com.zeotap.fanout.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeotap.fanout.model.Record;

public class JsonTransformer implements Transformer {

    private final ObjectMapper objectMapper;

    public JsonTransformer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object transform(Record record) {
        try {
            return objectMapper.writeValueAsString(record.data());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to transform record to JSON", e);
        }
    }
}
