package com.training.kafka.Day06Schemas;

import com.training.kafka.avro.UserEvent;
import com.training.kafka.avro.ActionType;
import com.training.kafka.avro.DeviceInfo;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Day 6: Avro Producer
 * 
 * Demonstrates schema-based message production using Apache Avro.
 * Shows schema evolution, compatibility, and Schema Registry integration.
 */
public class AvroProducer {
    private static final Logger logger = LoggerFactory.getLogger(AvroProducer.class);
    
    private final KafkaProducer<String, UserEvent> producer;
    private final String topicName;
    
    public AvroProducer(String bootstrapServers, String schemaRegistryUrl, String topicName) {
        this.topicName = topicName;
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "avro-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        
        // Schema Registry configuration
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("auto.register.schemas", "true");
        props.put("use.latest.version", "true");
        
        // Producer settings for reliability
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        this.producer = new KafkaProducer<>(props);
        logger.info("Avro producer initialized for topic: {}", topicName);
    }
    
    /**
     * Send a user event using Avro schema
     */
    public void sendUserEvent(String userId, ActionType action, Map<String, String> properties) {
        try {
            // Create device info
            DeviceInfo deviceInfo = DeviceInfo.newBuilder()
                .setDeviceType("web")
                .setUserAgent("Mozilla/5.0 (training)")
                .setIpAddress("192.168.1.100")
                .setLocation("Training Center")
                .build();
            
            // Create user event
            UserEvent userEvent = UserEvent.newBuilder()
                .setUserId(userId)
                .setAction(action)
                .setTimestamp(Instant.now().toEpochMilli())
                .setSessionId("session_" + System.currentTimeMillis())
                .setProperties(properties != null ? properties : new HashMap<>())
                .setDeviceInfo(deviceInfo)
                .setVersion(1)
                .build();
            
            ProducerRecord<String, UserEvent> record = 
                new ProducerRecord<>(topicName, userId, userEvent);
            
            // Send asynchronously with callback
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Sent Avro message: topic={}, partition={}, offset={}, key={}", 
                        metadata.topic(), metadata.partition(), metadata.offset(), userId);
                } else {
                    logger.error("Failed to send Avro message for user {}: {}", userId, exception.getMessage());
                }
            });
            
        } catch (Exception e) {
            logger.error("Error creating Avro user event", e);
        }
    }
    
    /**
     * Send a batch of user events
     */
    public void sendBatchUserEvents(String[] userIds, ActionType[] actions) {
        logger.info("Sending batch of {} Avro user events", userIds.length);
        
        for (int i = 0; i < userIds.length; i++) {
            Map<String, String> properties = new HashMap<>();
            properties.put("batch_index", String.valueOf(i));
            properties.put("source", "batch_producer");
            
            sendUserEvent(userIds[i], actions[i % actions.length], properties);
        }
        
        producer.flush();
        logger.info("Batch sending completed");
    }
    
    /**
     * Demonstrate schema evolution by sending different versions
     */
    public void demonstrateSchemaEvolution() {
        logger.info("Demonstrating schema evolution...");
        
        // Send basic event (v1 schema)
        Map<String, String> basicProps = new HashMap<>();
        basicProps.put("version_demo", "v1_basic");
        sendUserEvent("evolution_user1", ActionType.LOGIN, basicProps);
        
        // Send enriched event (v1 schema with more fields)
        Map<String, String> enrichedProps = new HashMap<>();
        enrichedProps.put("version_demo", "v1_enriched");
        enrichedProps.put("experiment_group", "A");
        enrichedProps.put("feature_flags", "new_ui,analytics");
        sendUserEvent("evolution_user2", ActionType.PURCHASE, enrichedProps);
        
        producer.flush();
        logger.info("Schema evolution demonstration completed");
    }
    
    public void close() {
        producer.close();
        logger.info("Avro producer closed");
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String schemaRegistryUrl = "http://localhost:8081";
        String topicName = "avro-user-events";
        
        AvroProducer producer = new AvroProducer(bootstrapServers, schemaRegistryUrl, topicName);
        
        try {
            // Send individual events
            Map<String, String> loginProps = new HashMap<>();
            loginProps.put("login_method", "password");
            loginProps.put("device_fingerprint", "abc123");
            producer.sendUserEvent("user1", ActionType.LOGIN, loginProps);
            
            Map<String, String> purchaseProps = new HashMap<>();
            purchaseProps.put("product_id", "prod_456");
            purchaseProps.put("amount", "99.99");
            purchaseProps.put("currency", "USD");
            producer.sendUserEvent("user2", ActionType.PURCHASE, purchaseProps);
            
            // Send batch events
            String[] userIds = {"user3", "user4", "user5", "user6"};
            ActionType[] actions = {ActionType.PAGE_VIEW, ActionType.SEARCH, ActionType.CART_ADD};
            producer.sendBatchUserEvents(userIds, actions);
            
            // Demonstrate schema evolution
            producer.demonstrateSchemaEvolution();
            
            Thread.sleep(2000); // Wait for async sends
            
        } catch (Exception e) {
            logger.error("Error in main", e);
        } finally {
            producer.close();
        }
    }
}