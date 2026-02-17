package com.zeotap.fanout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zeotap.fanout.config.AppConfig;
import com.zeotap.fanout.model.Record;
import com.zeotap.fanout.sink.*;
import com.zeotap.fanout.source.CsvFileSource;
import com.zeotap.fanout.source.JsonlFileSource;
import com.zeotap.fanout.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class FanOutEngine {
    private static final Logger logger = LoggerFactory.getLogger(FanOutEngine.class);

    public static void main(String[] args) {
        try {
            AppConfig config;
            if (args.length < 1) {
                logger.info("No config file provided. Using default configuration.");
                config = createDefaultConfig();
            } else {
                config = loadConfig(args[0]);
            }
            new FanOutEngine().run(config);
        } catch (Exception e) {
            logger.error("Application failed", e);
            System.exit(1);
        }
    }

    private static AppConfig loadConfig(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(path), AppConfig.class);
    }

    private static AppConfig createDefaultConfig() {
        AppConfig config = new AppConfig();
        
        // Default Source
        AppConfig.SourceConfig source = new AppConfig.SourceConfig();
        source.type = "csv";
        source.path = "data/input.csv";
        config.source = source;

        // Default Sinks
        config.sinks = new ArrayList<>();
        
        config.sinks.add(createSinkConfig("rest-sink", "rest", 50.0));
        config.sinks.add(createSinkConfig("grpc-sink", "grpc", 100.0));
        config.sinks.add(createSinkConfig("mq-sink", "mq", 20.0));
        config.sinks.add(createSinkConfig("db-sink", "db", 10.0));
        
        return config;
    }

    private static AppConfig.SinkConfig createSinkConfig(String name, String type, double rateLimit) {
        AppConfig.SinkConfig sink = new AppConfig.SinkConfig();
        sink.name = name;
        sink.type = type;
        sink.rateLimit = rateLimit;
        return sink;
    }

    public void run(AppConfig config) throws Exception {
        // 1. Setup Sinks
        List<Sink> sinks = new ArrayList<>();
        for (AppConfig.SinkConfig sinkConfig : config.sinks) {
            Sink sink = createSink(sinkConfig);
            if (sinkConfig.rateLimit > 0) {
                sink = new ThrottledSink(sink, sinkConfig.rateLimit);
            }
            sinks.add(sink);
        }

        // 2. Setup Executor (Virtual Threads)
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // 3. Metrics
        AtomicLong processedCount = new AtomicLong(0);
        java.util.concurrent.ConcurrentHashMap<String, AtomicLong> successCounts = new java.util.concurrent.ConcurrentHashMap<>();
        java.util.concurrent.ConcurrentHashMap<String, AtomicLong> failureCounts = new java.util.concurrent.ConcurrentHashMap<>();
        
        // Initialize counters
        for (Sink sink : sinks) {
            successCounts.put(sink.getName(), new AtomicLong(0));
            failureCounts.put(sink.getName(), new AtomicLong(0));
        }

        long startTime = System.currentTimeMillis();
        ScheduledExecutorService metricsExecutor = Executors.newSingleThreadScheduledExecutor();
        metricsExecutor.scheduleAtFixedRate(() -> {
            long count = processedCount.get();
            long elapsed = System.currentTimeMillis() - startTime;
            double throughput = (double) count / elapsed * 1000;
            logger.info("Processed: {} records. Throughput: {:.2f} rec/sec", count, throughput);
            
            StringBuilder stats = new StringBuilder("Sink Stats:\n");
            for (Sink sink : sinks) {
                stats.append(String.format("  - %s: Success=%d, Failure=%d%n",
                        sink.getName(),
                        successCounts.get(sink.getName()).get(),
                        failureCounts.get(sink.getName()).get()));
            }
            logger.info(stats.toString());
        }, 5, 5, TimeUnit.SECONDS);

        // DLQ Logger
        Logger dlqLogger = LoggerFactory.getLogger("DLQ");

        // 4. Ingestion & Distribution
        Source source = createSource(config.source);
        try (Stream<Record> records = source.read()) {
            records.parallel().forEach(record -> {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (Sink sink : sinks) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        Object transformed = sink.getTransformer().transform(record);
                        boolean sent = false;
                        int attempts = 0;
                        while (!sent && attempts < 3) { // Max 3 retries
                            attempts++;
                            try {
                                sink.process(transformed);
                                sent = true;
                                successCounts.get(sink.getName()).incrementAndGet();
                            } catch (Exception e) {
                                logger.warn("Sink {} failed (Attempt {}/3): {}", sink.getName(), attempts, e.getMessage());
                                if (attempts < 3) {
                                    try {
                                        Thread.sleep(100 * attempts); // Exponential backoff
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }
                        }
                        
                        if (!sent) {
                            failureCounts.get(sink.getName()).incrementAndGet();
                            logger.error("Sink {} failed permanently for record: {}", sink.getName(), record);
                            dlqLogger.error("FailedRecord: Sink={}, Data={}", sink.getName(), record);
                        }
                    }, executor));
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                processedCount.incrementAndGet();
            });
        }

        // Shutdown
        metricsExecutor.shutdown();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private Sink createSink(AppConfig.SinkConfig config) {
        switch (config.type.toLowerCase()) {
            case "rest":
                return new RestSink();
            case "grpc":
                return new GrpcSink();
            case "mq":
                return new MqSink();
            case "db":
                return new DbSink();
            default:
                throw new IllegalArgumentException("Unknown sink type: " + config.type);
        }
    }

    private Source createSource(AppConfig.SourceConfig config) {
        Path paths = Paths.get(config.path);
        switch (config.type.toLowerCase()) {
            case "csv":
                return new CsvFileSource(paths);
            case "jsonl":
                return new JsonlFileSource(paths);
            default:
                throw new IllegalArgumentException("Unknown source type: " + config.type);
        }
    }
}
