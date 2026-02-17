package com.zeotap.fanout.sink;

import com.zeotap.fanout.transform.Transformer;

public interface Sink {
    void process(Object data);

    Transformer getTransformer();

    String getName();
}
