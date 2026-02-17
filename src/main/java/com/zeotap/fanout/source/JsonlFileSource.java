package com.zeotap.fanout.source;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeotap.fanout.model.Record;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonlFileSource implements Source {

    private final Path filePath;
    private final ObjectMapper objectMapper;

    public JsonlFileSource(Path filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Stream<Record> read() throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(filePath.toFile());
        MappingIterator<Map<String, Object>> iterator = objectMapper.readValues(parser,
                new TypeReference<Map<String, Object>>() {
                });

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false)
                .map(Record::new)
                .onClose(() -> {
                    try {
                        parser.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to close JSON parser", e);
                    }
                });
    }
}
