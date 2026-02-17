package com.zeotap.fanout.source;

import com.zeotap.fanout.model.Record;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CsvFileSource implements Source {

    private final Path filePath;

    public CsvFileSource(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Stream<Record> read() throws IOException {
        BufferedReader reader = Files.newBufferedReader(filePath);
        CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader);

        return StreamSupport.stream(parser.spliterator(), false)
                .map(csvRecord -> {
                    Map<String, Object> map = new java.util.HashMap<>(csvRecord.toMap());
                    return new Record(map);
                })
                .onClose(() -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to close CSV reader", e);
                    }
                });
    }
}
