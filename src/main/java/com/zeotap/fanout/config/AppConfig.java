package com.zeotap.fanout.config;

import java.util.List;

public class AppConfig {
    public SourceConfig source;
    public List<SinkConfig> sinks;

    public static class SourceConfig {
        public String type;
        public String path;
    }

    public static class SinkConfig {
        public String name;
        public String type;
        public double rateLimit;
    }
}
