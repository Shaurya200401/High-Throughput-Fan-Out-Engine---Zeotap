package com.zeotap.fanout.sink;

import com.zeotap.fanout.transform.Transformer;
import com.zeotap.fanout.transform.XmlTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqSink implements Sink {
    private static final Logger logger = LoggerFactory.getLogger(MqSink.class);
    private final Transformer transformer = new XmlTransformer();

    @Override
    public void process(Object data) {
        // Mock MQ Publish
        String xml = (String) data;
        // Simulate network latency
        try {
            Thread.sleep(15); // 15ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.debug("MQ Sink processed: {}", xml);
    }

    @Override
    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public String getName() {
        return "MqSink";
    }
}
