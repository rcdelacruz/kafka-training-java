# Day 6: Schema Management with Apache Avro

## Learning Objectives
By the end of Day 6, you will:
- Understand schema evolution and compatibility
- Implement Avro producers and consumers
- Work with Schema Registry
- Handle different schema versions gracefully
- Implement data governance best practices

## Morning Session (3 hours): Schema Theory and Avro Basics

### 1. Why Schema Management?

**Benefits of Schema Management:**
- **Data Quality**: Enforce structure and types
- **Evolution**: Handle changes without breaking consumers
- **Documentation**: Schema serves as API contract
- **Efficiency**: Binary serialization is faster and smaller
- **Compatibility**: Ensure forward/backward compatibility

### 2. Apache Avro Overview

**Key Features:**
- Rich data structures
- Compact, fast binary data format
- Container file to store persistent data
- RPC (Remote Procedure Call)
- Simple integration with dynamic languages

**Schema Evolution Types:**
- **Forward Compatibility**: New schema can read old data
- **Backward Compatibility**: Old schema can read new data
- **Full Compatibility**: Both forward and backward compatible

### 3. Schema Registry

**What is Schema Registry?**
- Centralized repository for schemas
- RESTful interface for storing and retrieving schemas
- Schema versioning and compatibility checking
- Integration with Kafka ecosystem

**Key Concepts:**
- **Subject**: Named container for schema versions
- **Schema ID**: Unique identifier for each schema version
- **Compatibility Level**: Rules for schema evolution

## Afternoon Session (3 hours): Hands-on Implementation

### 1. Set Up Schema Registry

```bash
# Start Schema Registry (if using Docker)
docker-compose up -d schema-registry

# Or start with Confluent CLI
confluent local schema-registry start

# Verify Schema Registry is running
curl http://localhost:8081/subjects
```

### 2. Avro Schema Development

#### Our User Event Schema (Already Created)
```json
{
  "type": "record",
  "name": "UserEvent",
  "namespace": "com.training.kafka.avro",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": {"type": "enum", "name": "ActionType", "symbols": ["LOGIN", "LOGOUT", "PURCHASE", "PAGE_VIEW", "SEARCH", "CART_ADD", "CART_REMOVE"]}},
    {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"},
    {"name": "sessionId", "type": "string"},
    {"name": "properties", "type": {"type": "map", "values": "string"}, "default": {}},
    {"name": "deviceInfo", "type": ["null", {"type": "record", "name": "DeviceInfo", "fields": [...]}], "default": null},
    {"name": "version", "type": "int", "default": 1}
  ]
}
```

### 3. Generate Avro Classes

```bash
# Generate Java classes from schema
mvn avro:schema

# This creates classes in target/generated-sources/avro/
# - com.training.kafka.avro.UserEvent
# - com.training.kafka.avro.ActionType
# - com.training.kafka.avro.DeviceInfo
```

### 4. Avro Producer Implementation

Run the Avro Producer example:

```bash
# Create Avro topic
confluent local kafka topic create avro-user-events \
  --partitions 3 \
  --replication-factor 1

# Run Avro Producer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroProducer"
```

**Key Implementation Details:**

1. **Schema Registry Configuration**
```java
props.put("schema.registry.url", "http://localhost:8081");
props.put("auto.register.schemas", "true");
props.put("use.latest.version", "true");
```

2. **Avro Serializer**
```java
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
```

3. **Creating Avro Objects**
```java
UserEvent userEvent = UserEvent.newBuilder()
    .setUserId(userId)
    .setAction(action)
    .setTimestamp(Instant.now().toEpochMilli())
    .setSessionId("session_" + System.currentTimeMillis())
    .setProperties(properties)
    .setDeviceInfo(deviceInfo)
    .setVersion(1)
    .build();
```

### 5. Avro Consumer Implementation

Run the Avro Consumer example:

```bash
# Run Avro Consumer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroConsumer"

# Run with schema evolution demo
mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroConsumer" -Dexec.args="schema-evolution"
```

**Key Implementation Details:**

1. **Specific Avro Reader**
```java
props.put("specific.avro.reader", "true");
```

2. **Type-Safe Consumption**
```java
KafkaConsumer<String, UserEvent> consumer = new KafkaConsumer<>(props);
UserEvent userEvent = record.value();
```

3. **Handling Optional Fields**
```java
if (userEvent.getDeviceInfo() != null) {
    logger.info("Device Type: {}", userEvent.getDeviceInfo().getDeviceType());
}
```

### 6. Schema Evolution Examples

#### Example 1: Adding Optional Field

**Original Schema (v1):**
```json
{
  "type": "record",
  "name": "UserEvent",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": "string"}
  ]
}
```

**Evolved Schema (v2) - Adding Optional Field:**
```json
{
  "type": "record", 
  "name": "UserEvent",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": "string"},
    {"name": "timestamp", "type": "long", "default": 0}
  ]
}
```

#### Example 2: Schema Evolution Best Practices

```bash
# Register schema manually
curl -X POST \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"UserEvent\"...}"}' \
  http://localhost:8081/subjects/avro-user-events-value/versions

# Check compatibility before evolving
curl -X POST \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"UserEvent\"...}"}' \
  http://localhost:8081/compatibility/subjects/avro-user-events-value/versions/latest
```

## Schema Registry Operations

### CLI Commands

```bash
# List all subjects
curl http://localhost:8081/subjects

# Get latest schema for subject
curl http://localhost:8081/subjects/avro-user-events-value/versions/latest

# Get all versions for subject
curl http://localhost:8081/subjects/avro-user-events-value/versions

# Set compatibility level
curl -X PUT \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "BACKWARD"}' \
  http://localhost:8081/config/avro-user-events-value

# Delete schema version
curl -X DELETE http://localhost:8081/subjects/avro-user-events-value/versions/1
```

### Compatibility Levels

1. **BACKWARD**: New schema can read data written with previous schema
2. **FORWARD**: Previous schema can read data written with new schema  
3. **FULL**: Both backward and forward compatible
4. **NONE**: No compatibility checking
5. **BACKWARD_TRANSITIVE**: Backward compatible with all previous versions
6. **FORWARD_TRANSITIVE**: Forward compatible with all previous versions
7. **FULL_TRANSITIVE**: Both backward and forward transitive

## Exercises

### Exercise 1: Basic Avro Producer/Consumer
1. Run the AvroProducer to send events
2. Run the AvroConsumer to receive and process events
3. Observe the structured data and type safety

### Exercise 2: Schema Evolution
1. Modify the UserEvent schema to add a new optional field
2. Generate new Java classes
3. Update producer to set the new field
4. Verify old consumers can still read new data
5. Update consumer to handle the new field

### Exercise 3: Schema Registry Operations
1. Register a schema manually via REST API
2. Check compatibility before making changes
3. Evolve the schema with a breaking change
4. Observe what happens when compatibility fails

### Exercise 4: Error Handling
1. Try sending malformed data
2. Handle schema registry unavailability
3. Deal with deserialization errors
4. Implement retry logic for schema operations

## Best Practices

### 1. Schema Design
- Use meaningful field names and documentation
- Design for evolution from the start
- Use unions sparingly (prefer optional fields)
- Include version fields for tracking
- Use logical types for dates, decimals, etc.

### 2. Compatibility Strategy
- Start with BACKWARD compatibility for most use cases
- Use FULL compatibility for critical schemas
- Plan schema evolution carefully
- Test compatibility before deploying changes

### 3. Schema Registry Management
- Use namespaces to organize schemas
- Implement CI/CD for schema changes
- Monitor schema registry health
- Back up schema registry data
- Use meaningful subject naming conventions

### 4. Development Workflow
- Generate classes in build process
- Version control your schemas
- Use schema validation in tests
- Document breaking changes clearly
- Coordinate schema changes across teams

## Performance Considerations

### 1. Serialization Performance
- Avro is faster than JSON for large objects
- Binary format reduces network overhead
- Schema caching improves performance
- Consider compression at topic level

### 2. Schema Registry Caching
- Enable client-side schema caching
- Monitor cache hit rates
- Size caches appropriately
- Handle cache refresh gracefully

### 3. Memory Usage
- Avro objects can be memory intensive
- Consider object pooling for high throughput
- Monitor JVM heap usage
- Use specific readers when possible

## Troubleshooting

### Common Issues

1. **Schema not found**
   - Check Schema Registry connectivity
   - Verify subject name matches
   - Ensure schema is registered

2. **Incompatible schema changes**
   - Check compatibility rules
   - Use schema validation before registration
   - Plan evolution path carefully

3. **Deserialization errors**
   - Verify schema versions match
   - Check for corrupted data
   - Validate schema registry health

4. **Performance issues**
   - Monitor schema caching
   - Check network latency to Schema Registry
   - Optimize schema design

## Key Takeaways

1. **Schema management is crucial** for data governance and evolution
2. **Avro provides excellent schema evolution** capabilities
3. **Schema Registry centralizes** schema management and compatibility
4. **Forward/backward compatibility** enables smooth schema evolution
5. **Type safety and performance** are major benefits of Avro
6. **Plan for evolution** from the beginning of your schema design

## Next Steps

Tomorrow we'll explore:
- Kafka Connect for data integration
- Source and sink connectors
- Custom connector development
- Data pipeline patterns

---

**🚀 Ready for Day 7?** Continue with [Day 7: Kafka Connect](./day07-connect.md)