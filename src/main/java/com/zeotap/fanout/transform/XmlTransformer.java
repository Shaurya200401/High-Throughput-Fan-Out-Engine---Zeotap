package com.zeotap.fanout.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.zeotap.fanout.model.Record;

import java.util.Map;

public class XmlTransformer implements Transformer {

    private final XmlMapper xmlMapper;

    public XmlTransformer() {
        this.xmlMapper = new XmlMapper();
    }

    @Override
    public Object transform(Record record) {
        try {
            return xmlMapper.writeValueAsString(record.data());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to transform record to XML", e);
        }
    }
}
