package com.zeotap.fanout.transform;

import com.zeotap.fanout.model.Record;
import com.zeotap.fanout.proto.ProtoRecord;

import java.util.Map;

public class ProtobufTransformer implements Transformer {

    @Override
    public Object transform(Record record) {
        ProtoRecord.Builder builder = ProtoRecord.newBuilder();
        for (Map.Entry<String, Object> entry : record.data().entrySet()) {
            builder.putData(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return builder.build();
    }
}
