# RA2211003010438 Shaurya Awasthi
# Distributed Data Fan-Out & Transformation Engine

A high-throughput Java application that reads records from a file source and distributes them to multiple mock sinks (Rest, gRPC, MQ, DB) with different formats.

## Features
- **Streaming Ingestion**: Handles large files (100GB+) with low memory footprint.
- **Concurrent Processing**: Uses Java Virtual Threads for high throughput.
- **Transformation Strategy**: Converts data to JSON, Protobuf, XML, Avro.
- **Resilience**: Backpressure handling, Rate Limiting, and Retry logic.
- **Observability**: Real-time throughput and status logging.

## Requirements
- Java 21+
- Gradle

## Setup
1. Clone the repository.
2. Run `mvn clean install` to build the project.
3. Run `java -jar target/fanout-engine-1.0-SNAPSHOT.jar` to start the application (uses default config).
    - Optional: `java -jar target/fanout-engine-1.0-SNAPSHOT.jar <config-file>`

## Architecture
The system follows a **Fan-Out** architecture using the **Strategy Pattern** for transformations and sinks.

1.  **Ingestion**: `Source` reads files line-by-line (streaming) to keep memory usage low.
2.  **Transformation**: `Transformer` converts the raw record into the target sink format (JSON, Avro, etc.).
3.  **Distribution**: `Sink` sends the data to the destination (Mocked).
    -   Wrapped in `ThrottledSink` for rate limiting.
    -   Executed in parallel using **Virtual Threads**.
4.  **Resilience**:
    -   **Retries**: 3 attempts with exponential backoff for failed sink operations.
    -   **DLQ**: Failed records are logged to a DLQ logger.

## Design Decisions
1.  **Concurrency**: Chosen **Java Virtual Threads** (Project Loom) because the workload is I/O intensive (sending data to sinks). Virtual threads allow high throughput without the overhead of native OS threads.
2.  **Backpressure**: currently handled implicitly by the `Stream` pipeline and `CompletableFuture.allOf()`. This blocks the main ingestion thread if s flush is needed, preventing the system from reading faster than it can process.
3.  **Strategy Pattern**: Used for `Source`, `Sink`, and `Transformer` to make the system easily extensible (Open/Closed Principle). Adding a new Sink requires zero changes to the core engine.

## Assumptions
1.  **Input Format**: specific CSV/JSONL structure is expected as per `Record` model.
2.  **Network Latency**: Simulated using `Thread.sleep()` in mock sinks.
3.  **Crash Recovery**: The current state is in-memory. If the application crashes, in-flight records might be lost (though the source offset could be implemented for full recovery).

## Prompts Used

### Initial Planning
- "Create a task list for building a High-Throughput Fan-Out Engine."
- "Design a strategy pattern for multiple Sinks (REST, gRPC, MQ, DB) in Java."
- "Suggest a project structure for a Gradle-based Java 21 application."

### Implementation
- "Implement a `JsonlFileSource` that reads a large file stream-wise using Jackson."
- "Create a `ThrottledSink` wrapper using Guava RateLimiter."
- "How to use Java 21 Virtual Threads for concurrent sink processing?"
- "Implement a mock REST sink that simulates network latency."

### Debugging & Refinement
- "Add a retry mechanism with exponential backoff for the sink process method."
- "Implement a Dead Letter Queue (DLQ) logger for failed records."
- "How to track success vs failure counts per sink in a thread-safe way?"

### Automatic Docs Generation
- "Generate a Mermaid diagram for the Fan-Out architecture."
- "Write a README.md explaining the design decisions regarding backpressure."
