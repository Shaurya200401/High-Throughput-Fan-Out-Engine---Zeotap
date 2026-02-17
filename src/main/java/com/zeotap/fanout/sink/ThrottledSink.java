package com.zeotap.fanout.sink;

import com.google.common.util.concurrent.RateLimiter;
import com.zeotap.fanout.transform.Transformer;

public class ThrottledSink implements Sink {

    private final Sink delegate;
    private final RateLimiter rateLimiter;

    public ThrottledSink(Sink delegate, double recordsPerSecond) {
        this.delegate = delegate;
        this.rateLimiter = RateLimiter.create(recordsPerSecond);
    }

    @Override
    public void process(Object data) {
        rateLimiter.acquire();
        delegate.process(data);
    }

    @Override
    public Transformer getTransformer() {
        return delegate.getTransformer();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
