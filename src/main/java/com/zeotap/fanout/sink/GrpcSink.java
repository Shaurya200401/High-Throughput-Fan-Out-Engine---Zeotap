package com.zeotap.fanout.sink;

import com.zeotap.fanout.transform.ProtobufTransformer;
import com.zeotap.fanout.transform.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcSink implements Sink {
    private static final Logger logger = LoggerFactory.getLogger(GrpcSink.class);
    private final Transformer transformer = new ProtobufTransformer();

    @Override
    public void process(Object data) {
        // Mock gRPC call
        // Object protobufMessage = data;
        // Simulate network latency
        try {
            Thread.sleep(10); // 10ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.debug("gRPC Sink processed message");
    }

    @Override
    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public String getName() {
        return "GrpcSink";
    }
}
