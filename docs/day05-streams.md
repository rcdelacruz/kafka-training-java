# Day 5: Stream Processing with Kafka Streams

## Learning Objectives
By the end of Day 5, you will:
- Understand Kafka Streams architecture and concepts
- Implement real-time stream transformations
- Build stateful stream processing applications
- Create stream-stream and stream-table joins
- Handle windowing and aggregations
- Deploy fault-tolerant streaming applications

## Morning Session (3 hours): Kafka Streams Fundamentals

### 1. Stream Processing Concepts

**What is Stream Processing?**
- Continuous processing of data streams
- Event-driven, real-time analytics
- Stateful computations over infinite data sets
- Low-latency responses to events

**Kafka Streams vs Other Systems:**
- Embedded library (not separate cluster)
- Exactly-once semantics
- Fault tolerance via Kafka
- No external dependencies

### 2. Core Abstractions

#### KStream (Event Stream)
```java
// Represents a stream of records (events)
KStream<String, String> userEvents = builder.stream("user-events");

// Each record is an independent event
// Suitable for: logs, transactions, user actions
```

#### KTable (Changelog Stream)
```java
// Represents a table of latest values per key
KTable<String, String> userProfiles = builder.table("user-profiles");

// Updates replace previous values for same key
// Suitable for: user profiles, configurations, aggregations
```

#### GlobalKTable
```java
// Replicated to all application instances
GlobalKTable<String, String> productCatalog = builder.globalTable("products");

// Available for joins without repartitioning
// Suitable for: reference data, lookup tables
```

### 3. Stream Processing Topology

```java
StreamsBuilder builder = new StreamsBuilder();

KStream<String, String> source = builder.stream("input-topic");

KStream<String, String> processed = source
    .filter((key, value) -> value.contains("important"))
    .mapValues(value -> value.toUpperCase())
    .groupByKey()
    .count()
    .toStream()
    .mapValues(count -> "Count: " + count);

processed.to("output-topic");

Topology topology = builder.build();
```

## Afternoon Session (3 hours): Real-time Processing Patterns

### Exercise 1: Basic Stream Transformations

Run the Stream Processor example:

```bash
# Start the stream processor
mvn exec:java -Dexec.mainClass="com.training.kafka.Day05Streams.StreamProcessor"
```

The example demonstrates:
- **Filtering**: Remove irrelevant events
- **Transformation**: Enrich and modify events
- **Branching**: Split streams based on conditions
- **Joining**: Combine related streams

### Exercise 2: Stateful Processing - Aggregations

#### Count Events by User
```java
KStream<String, String> userEvents = builder.stream("user-events");

KTable<String, Long> userEventCounts = userEvents
    .groupByKey()
    .count();

// Convert back to stream for output
userEventCounts
    .toStream()
    .mapValues(count -> "User has " + count + " events")
    .to("user-event-counts");
```

#### Windowed Aggregations
```java
KTable<Windowed<String>, Long> windowedCounts = userEvents
    .groupByKey()
    .windowedBy(TimeWindows.of(Duration.ofMinutes(5))) // 5-minute windows
    .count();

windowedCounts
    .toStream()
    .map((windowedKey, count) -> KeyValue.pair(
        windowedKey.key() + "@" + windowedKey.window().start(),
        "Count in window: " + count
    ))
    .to("windowed-counts");
```

### Exercise 3: Stream-Stream Joins

#### Inner Join (Both streams must have matching keys)
```java
KStream<String, String> leftStream = builder.stream("left-topic");
KStream<String, String> rightStream = builder.stream("right-topic");

KStream<String, String> joinedStream = leftStream.join(
    rightStream,
    (leftValue, rightValue) -> leftValue + " + " + rightValue,
    JoinWindows.of(Duration.ofMinutes(5)) // Join window
);

joinedStream.to("joined-output");
```

## Performance Optimization

### 1. Parallelism
```java
// Scale processing by increasing topic partitions
Properties props = new Properties();
props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "4"); // 4 threads
```

### 2. State Store Optimization
```java
// Configure state store caching
props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024); // 10MB
props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000); // Commit every 30s
```

## Testing Stream Processing

### 1. Topology Test Driver
```java
@Test
public void testWordCountTopology() {
    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> source = builder.stream("input");
    
    KTable<String, Long> wordCounts = source
        .flatMapValues(value -> Arrays.asList(value.toLowerCase().split(" ")))
        .groupBy((key, word) -> word)
        .count();
    
    wordCounts.toStream().to("output");
    
    try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), props)) {
        TestInputTopic<String, String> inputTopic = 
            testDriver.createInputTopic("input", Serdes.String().serializer(), 
                Serdes.String().serializer());
        TestOutputTopic<String, Long> outputTopic = 
            testDriver.createOutputTopic("output", Serdes.String().deserializer(), 
                Serdes.Long().deserializer());
        
        // Send test data
        inputTopic.pipeInput("key1", "hello world hello");
        
        // Verify output
        KeyValue<String, Long> result = outputTopic.readKeyValue();
        assertEquals("hello", result.key);
        assertEquals(2L, result.value);
    }
}
```

## Real-World Use Cases

### 1. Real-time Recommendations
```java
// Join user actions with product catalog for recommendations
KStream<String, String> userActions = builder.stream("user-actions");
GlobalKTable<String, String> products = builder.globalTable("products");

KStream<String, String> recommendations = userActions
    .join(products,
        (userId, action) -> extractProductId(action), // Key mapper
        (action, product) -> generateRecommendation(action, product))
    .to("recommendations");
```

### 2. IoT Data Processing
```java
// Process sensor data for anomaly detection
KStream<String, String> sensorData = builder.stream("sensor-readings");

KTable<String, Double> averages = sensorData
    .map((key, value) -> KeyValue.pair(extractSensorId(value), extractTemperature(value)))
    .groupByKey()
    .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
    .aggregate(
        () -> new TemperatureStats(),
        (key, temp, stats) -> stats.addReading(temp),
        Materialized.with(Serdes.String(), temperatureStatsSerde)
    )
    .mapValues(stats -> stats.getAverage());

// Alert on anomalies
KStream<String, String> alerts = averages
    .toStream()
    .filter((windowedKey, avg) -> avg > 85.0) // Temperature threshold
    .map((windowedKey, avg) -> KeyValue.pair(
        windowedKey.key(),
        createAlert(windowedKey.key(), avg)
    ));

alerts.to("temperature-alerts");
```

### 3. Financial Risk Management
```java
// Monitor trading patterns for risk
KStream<String, String> trades = builder.stream("trades");

KTable<String, Double> positionSizes = trades
    .map((key, trade) -> KeyValue.pair(extractUserId(trade), extractAmount(trade)))
    .groupByKey()
    .aggregate(
        () -> 0.0,
        (userId, amount, total) -> total + amount,
        Materialized.with(Serdes.String(), Serdes.Double())
    );

// Alert on large positions
KStream<String, String> riskAlerts = positionSizes
    .toStream()
    .filter((userId, position) -> position > 1000000) // $1M threshold
    .mapValues(position -> createRiskAlert(position));

riskAlerts.to("risk-alerts");
```

## Production Deployment

### 1. Configuration for Production
```java
Properties props = new Properties();
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-streams-app");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "broker1:9092,broker2:9092");

// Reliability
props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);

// Performance
props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "4");
props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024);

// Monitoring
props.put(StreamsConfig.METRICS_RECORDING_LEVEL_CONFIG, "INFO");
```

### 2. Monitoring and Alerting
```java
public class StreamsMonitor {
    public void monitorStreamsApp(KafkaStreams streams) {
        // Monitor application state
        KafkaStreams.State state = streams.state();
        logger.info("Streams application state: {}", state);
        
        if (state == KafkaStreams.State.ERROR) {
            logger.error("Streams application in ERROR state");
            // Send alert
        }
        
        // Monitor thread health
        streams.localThreadsMetadata().forEach(thread -> {
            logger.info("Thread {}: State={}, Tasks={}", 
                thread.threadName(), thread.threadState(), thread.activeTasks().size());
        });
        
        // Monitor lag
        streams.allMetadata().forEach(metadata -> {
            metadata.standbyTasks().forEach(task -> {
                // Check standby task lag
                long lag = calculateLag(task);
                if (lag > 10000) {
                    logger.warn("High lag detected: {} records", lag);
                }
            });
        });
    }
}
```

### 3. Scaling Strategies
```bash
# Horizontal scaling: Add more application instances
# Each instance will get different partitions

# Vertical scaling: Increase threads per instance
StreamsConfig.NUM_STREAM_THREADS_CONFIG = "8"

# Partition scaling: Increase topic partitions (requires restart)
kafka-topics --alter --topic user-events --partitions 12
```

## Key Takeaways

1. **Kafka Streams** provides powerful real-time processing capabilities
2. **Stateful processing** enables complex aggregations and joins
3. **Windowing** allows time-based analysis of streaming data
4. **Fault tolerance** is built-in through Kafka's replication
5. **Scaling** is achieved through partitioning and threading
6. **Testing** can be done efficiently with TopologyTestDriver
7. **Production deployment** requires careful configuration and monitoring

## Common Patterns Summary

| Pattern | Use Case | Implementation |
|---------|----------|----------------|
| Filter & Transform | Data cleansing | `.filter()` + `.mapValues()` |
| Aggregation | Counting, summing | `.groupByKey().count()` |
| Windowing | Time-based analysis | `.windowedBy(TimeWindows.of())` |
| Stream-Stream Join | Event correlation | `.join()` with time window |
| Stream-Table Join | Event enrichment | `.join()` with KTable |
| Branching | Stream splitting | `.split().branch()` |

## Next Steps

Tomorrow we'll explore:
- Schema management with Apache Avro
- Schema Registry integration
- Schema evolution strategies
- Data governance patterns

---

**🚀 Ready for Day 6?** Continue with [Day 6: Schema Management](./day06-schemas.md)