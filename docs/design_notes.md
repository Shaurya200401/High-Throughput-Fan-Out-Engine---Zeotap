# High-Throughput Fan-Out Engine - Design Notes

## 1. Architecture Overview

The Fan-Out Engine is designed to ingest data from a source (CSV/JSONL), transform it, and distribute it to multiple sinks (Database, Message Queue, REST API, gRPC) concurrently. The system emphasizes high throughput, reliability, and extendability.

### Core Components

1.  **Source**: Reads data from the configured input source (e.g., File System).
    *   *Implementation*: `CsvFileSource`, `JsonlFileSource`
    *   *Key Feature*: Lazy loading (Streams) to handle large files without OOM.

2.  **Fan-Out Engine**: The central coordinator.
    *   *Mechanism*: Uses Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`) for high-concurrency fan-out.
    *   *Reliability*: Implements retry logic with exponential backoff for transient failures.

3.  **Sinks**: Destinations for the data.
    *   *Types*: REST, gRPC, RabbitMQ, PostgreSQL.
    *   *Feature*: Rate limiting (`ThrottledSink`) using Token Bucket algorithm (Guava RateLimiter) to protect downstream systems.
    *   *Note*: Sinks are currently implemented as **in-memory mocks** simulating latency. No external infrastructure (Docker) is required.

4.  **Transformers**: Converts internal `Record` format to sink-specific formats (JSON, Avro, Protobuf).

## 2. Design Decisions

### Concurrency Model
We chose **Java 21 Virtual Threads** over Reactive Streams or standard Thread Pools for the following reasons:
*   **Simplicity**: Blocking code looks imperative but scales like reactive code.
*   **Throughput**: Efficiently handles thousands of concurrent I/O-bound tasks (sinks).

### Reliability
*   **Retries**: 3 retries with exponential backoff to handle temporary network blips.
*   **DLQ (Dead Letter Queue)**: Failed records are logged to a separate DLQ logger for post-mortem analysis and replay.

### Extensibility
*   **Interface-based Design**: Adding a new sink type requires only implementing the `Sink` interface and adding a config entry.

## 3. Configuration
The application is configured via `config.yaml`, allowing dynamic definition of sources, sinks, and rate limits without code changes.

```yaml
sinks:
  - name: rest-sink
    type: rest
    rateLimit: 50.0 # Requests per second
```
