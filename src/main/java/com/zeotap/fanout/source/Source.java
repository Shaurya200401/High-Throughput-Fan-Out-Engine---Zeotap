package com.zeotap.fanout.source;

import com.zeotap.fanout.model.Record;
import java.io.IOException;
import java.util.stream.Stream;

public interface Source {
    Stream<Record> read() throws IOException;
}
