package com.zeotap.fanout;

import com.zeotap.fanout.model.Record;
import com.zeotap.fanout.proto.ProtoRecord;
import com.zeotap.fanout.transform.AvroTransformer;
import com.zeotap.fanout.transform.JsonTransformer;
import com.zeotap.fanout.transform.ProtobufTransformer;
import com.zeotap.fanout.transform.XmlTransformer;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TransformerTest {

    @Test
    public void testJsonTransformer() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        Record record = new Record(data);
        JsonTransformer transformer = new JsonTransformer();
        String result = (String) transformer.transform(record);
        Assertions.assertEquals("{\"key\":\"value\"}", result);
    }

    @Test
    public void testProtobufTransformer() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        Record record = new Record(data);
        ProtobufTransformer transformer = new ProtobufTransformer();
        ProtoRecord result = (ProtoRecord) transformer.transform(record);
        Assertions.assertEquals("value", result.getDataMap().get("key"));
    }

    @Test
    public void testXmlTransformer() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        Record record = new Record(data);
        XmlTransformer transformer = new XmlTransformer();
        String result = (String) transformer.transform(record);
        Assertions.assertTrue(result.contains("<key>value</key>"));
    }

    @Test
    public void testAvroTransformer() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        Record record = new Record(data);
        AvroTransformer transformer = new AvroTransformer();
        GenericRecord result = (GenericRecord) transformer.transform(record);
        Map<String, String> map = (Map<String, String>) result.get("data");
        Object val = map.get("key");
        Assertions.assertEquals("value", val.toString());
    }
}
