package com.training.kafka.Day06Schemas;

import com.training.kafka.avro.UserEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Day 6: Avro Consumer
 * 
 * Demonstrates schema-based message consumption using Apache Avro.
 * Shows schema evolution handling and deserialization.
 */
public class AvroConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AvroConsumer.class);
    
    private final KafkaConsumer<String, UserEvent> consumer;
    private volatile boolean running = true;
    
    public AvroConsumer(String bootstrapServers, String schemaRegistryUrl, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "avro-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        
        // Schema Registry configuration
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.avro.reader", "true"); // Use specific Avro records
        
        // Consumer behavior
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        
        this.consumer = new KafkaConsumer<>(props);
        logger.info("Avro consumer initialized for group: {}", groupId);
    }
    
    /**
     * Consume Avro messages from topic
     */
    public void consumeUserEvents(String topicName) {
        logger.info("Starting to consume Avro messages from topic: {}", topicName);
        
        consumer.subscribe(Collections.singletonList(topicName));
        
        try {
            while (running) {
                ConsumerRecords<String, UserEvent> records = consumer.poll(Duration.ofMillis(1000));
                
                if (records.isEmpty()) {
                    continue;
                }
                
                logger.info("Received {} Avro records", records.count());
                
                for (ConsumerRecord<String, UserEvent> record : records) {
                    processUserEvent(record);
                }
                
                // Commit offsets after processing
                try {
                    consumer.commitSync();
                    logger.debug("Offsets committed successfully");
                } catch (Exception e) {
                    logger.error("Failed to commit offsets: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error in consumer loop: {}", e.getMessage(), e);
        } finally {
            consumer.close();
            logger.info("Avro consumer closed");
        }
    }
    
    /**
     * Process individual Avro user event
     */
    private void processUserEvent(ConsumerRecord<String, UserEvent> record) {
        UserEvent userEvent = record.value();
        
        logger.info("Processing Avro user event:");
        logger.info("  Partition: {}, Offset: {}", record.partition(), record.offset());
        logger.info("  User ID: {}", userEvent.getUserId());
        logger.info("  Action: {}", userEvent.getAction());
        logger.info("  Timestamp: {}", new java.util.Date(userEvent.getTimestamp()));
        logger.info("  Session ID: {}", userEvent.getSessionId());
        logger.info("  Properties: {}", userEvent.getProperties());
        logger.info("  Schema Version: {}", userEvent.getVersion());
        
        // Handle device info (optional field)
        if (userEvent.getDeviceInfo() != null) {
            logger.info("  Device Type: {}", userEvent.getDeviceInfo().getDeviceType());
            logger.info("  User Agent: {}", userEvent.getDeviceInfo().getUserAgent());
            logger.info("  IP Address: {}", userEvent.getDeviceInfo().getIpAddress());
            logger.info("  Location: {}", userEvent.getDeviceInfo().getLocation());
        }
        
        // Business logic based on action type
        switch (userEvent.getAction()) {
            case LOGIN:
                handleLogin(userEvent);
                break;
            case LOGOUT:
                handleLogout(userEvent);
                break;
            case PURCHASE:
                handlePurchase(userEvent);
                break;
            case PAGE_VIEW:
                handlePageView(userEvent);
                break;
            case SEARCH:
                handleSearch(userEvent);
                break;
            case CART_ADD:
            case CART_REMOVE:
                handleCartAction(userEvent);
                break;
            default:
                logger.warn("Unknown action type: {}", userEvent.getAction());
        }
    }
    
    private void handleLogin(UserEvent event) {
        logger.info("Processing login for user: {}", event.getUserId());
        // Example: Update user session, track login analytics
        if (event.getProperties().containsKey("login_method")) {
            logger.info("Login method: {}", event.getProperties().get("login_method"));
        }
    }
    
    private void handleLogout(UserEvent event) {
        logger.info("Processing logout for user: {}", event.getUserId());
        // Example: Close session, calculate session duration
    }
    
    private void handlePurchase(UserEvent event) {
        logger.info("Processing purchase for user: {}", event.getUserId());
        // Example: Update inventory, process payment, send confirmation
        if (event.getProperties().containsKey("amount")) {
            logger.info("Purchase amount: ${}", event.getProperties().get("amount"));
        }
    }
    
    private void handlePageView(UserEvent event) {
        logger.debug("Processing page view for user: {}", event.getUserId());
        // Example: Track analytics, update recommendations
    }
    
    private void handleSearch(UserEvent event) {
        logger.info("Processing search for user: {}", event.getUserId());
        // Example: Track search terms, update search analytics
    }
    
    private void handleCartAction(UserEvent event) {
        logger.info("Processing cart action {} for user: {}", event.getAction(), event.getUserId());
        // Example: Update cart state, track conversion funnel
    }
    
    /**
     * Consume events with schema evolution demonstration
     */
    public void demonstrateSchemaEvolution(String topicName, int maxMessages) {
        logger.info("Demonstrating schema evolution handling...");
        
        consumer.subscribe(Collections.singletonList(topicName));
        
        int messagesProcessed = 0;
        
        try {
            while (running && messagesProcessed < maxMessages) {
                ConsumerRecords<String, UserEvent> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, UserEvent> record : records) {
                    UserEvent event = record.value();
                    
                    logger.info("Schema evolution demo - Event version: {}", event.getVersion());
                    
                    // Handle different schema versions gracefully
                    try {
                        // All events should work regardless of version due to Avro's schema evolution
                        processUserEvent(record);
                        messagesProcessed++;
                        
                        if (messagesProcessed >= maxMessages) {
                            break;
                        }
                        
                    } catch (Exception e) {
                        logger.error("Error processing event with schema version {}: {}", 
                            event.getVersion(), e.getMessage());
                    }
                }
                
                consumer.commitSync();
            }
        } catch (Exception e) {
            logger.error("Error in schema evolution demo: {}", e.getMessage(), e);
        }
        
        logger.info("Schema evolution demo completed. Processed {} messages", messagesProcessed);
    }
    
    public void stop() {
        running = false;
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String schemaRegistryUrl = "http://localhost:8081";
        String topicName = "avro-user-events";
        String groupId = "avro-consumer-group";
        
        AvroConsumer consumer = new AvroConsumer(bootstrapServers, schemaRegistryUrl, groupId);
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received");
            consumer.stop();
        }));
        
        if (args.length > 0 && "schema-evolution".equals(args[0])) {
            // Demonstrate schema evolution
            consumer.demonstrateSchemaEvolution(topicName, 10);
        } else {
            // Normal consumption
            consumer.consumeUserEvents(topicName);
        }
    }
}