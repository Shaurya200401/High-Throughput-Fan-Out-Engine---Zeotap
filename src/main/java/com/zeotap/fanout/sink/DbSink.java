package com.zeotap.fanout.sink;

import com.zeotap.fanout.transform.AvroTransformer;
import com.zeotap.fanout.transform.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSink implements Sink {
    private static final Logger logger = LoggerFactory.getLogger(DbSink.class);
    private final Transformer transformer = new AvroTransformer();

    @Override
    public void process(Object data) {
        // Mock DB Upsert
        // GenericRecord avroRecord = (GenericRecord) data;
        // Simulate DB latency
        try {
            Thread.sleep(50); // 50ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.debug("DB Sink processed record");
    }

    @Override
    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public String getName() {
        return "DbSink";
    }
}
