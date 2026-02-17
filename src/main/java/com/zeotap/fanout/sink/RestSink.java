package com.zeotap.fanout.sink;

import com.zeotap.fanout.transform.JsonTransformer;
import com.zeotap.fanout.transform.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestSink implements Sink {
    private static final Logger logger = LoggerFactory.getLogger(RestSink.class);
    private final Transformer transformer = new JsonTransformer();

    @Override
    public void process(Object data) {
        // Mock HTTP POST request
        String json = (String) data;
        // Simulate network latency
        try {
            Thread.sleep(20); // 20ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.debug("REST Sink processed: {}", json);
    }

    @Override
    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public String getName() {
        return "RestSink";
    }
}
