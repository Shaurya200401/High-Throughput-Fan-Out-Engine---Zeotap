package com.zeotap.fanout.transform;

import com.zeotap.fanout.model.Record;

public interface Transformer {
    Object transform(Record record);
}
