package com.training.kafka.services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Service for Day 4 Consumer Training
 * 
 * This service demonstrates Kafka message consumption using both Spring Kafka listeners
 * and raw Kafka consumers for educational purposes.
 * 
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day04ConsumerService {
    
    private static final Logger logger = LoggerFactory.getLogger(Day04ConsumerService.class);
    
    private final ConsumerFactory<String, String> consumerFactory;
    private final AtomicInteger messageCount = new AtomicInteger(0);
    
    /**
     * Constructor with dependency injection of Spring-managed Consumer Factory
     */
    public Day04ConsumerService(@Qualifier("trainingConsumerFactory") ConsumerFactory<String, String> consumerFactory) {
        this.consumerFactory = consumerFactory;
        logger.info("🎓 Day04ConsumerService initialized with Spring-managed ConsumerFactory");
    }
    
    /**
     * Spring Kafka Listener for automatic message consumption
     * This demonstrates the easiest way to consume messages in Spring Boot
     */
    @KafkaListener(
        topics = "user-events", 
        groupId = "spring-training-group",
        containerFactory = "trainingKafkaListenerContainerFactory"
    )
    public void listenToUserEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("📨 Received message via Spring Listener: topic={}, partition={}, offset={}, key={}, value={}", 
                topic, partition, offset, key, message);
            
            // Process the message
            processUserEvent(message);
            
            // Manually acknowledge the message
            acknowledgment.acknowledge();
            
            int count = messageCount.incrementAndGet();
            logger.debug("✅ Processed message #{} and acknowledged", count);
            
        } catch (Exception e) {
            logger.error("❌ Error processing message: {}", e.getMessage(), e);
            // In production, you might want to implement retry logic or send to DLQ
        }
    }
    
    /**
     * Spring Kafka Listener for demo topics
     */
    @KafkaListener(
        topics = {"demo-topic-1", "demo-topic-2"}, 
        groupId = "demo-training-group",
        containerFactory = "trainingKafkaListenerContainerFactory"
    )
    public void listenToDemoTopics(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            Acknowledgment acknowledgment) {
        
        logger.info("🎯 Demo message received: topic={}, partition={}, offset={}, key={}, value={}", 
            topic, partition, offset, key, message);
        
        acknowledgment.acknowledge();
    }
    
    /**
     * Consume messages using raw Kafka Consumer (for educational purposes)
     */
    public void consumeMessagesRaw(String topic, String groupId, int maxMessages) {
        logger.info("🔄 Starting raw consumer for topic '{}' with group '{}'", topic, groupId);
        
        // Create consumer with custom group ID
        Map<String, Object> consumerProps = consumerFactory.getConfigurationProperties();
        consumerProps.put("group.id", groupId);
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            // Subscribe to topic
            consumer.subscribe(Collections.singletonList(topic));
            
            int messageCount = 0;
            
            while (messageCount < maxMessages) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                if (records.isEmpty()) {
                    logger.debug("⏳ No messages received, continuing to poll...");
                    continue;
                }
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        logger.info("📨 Raw consumer received: topic={}, partition={}, offset={}, key={}, value={}", 
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());
                        
                        processMessage(record);
                        messageCount++;
                        
                        // Commit offset for this specific record
                        TopicPartition partition = new TopicPartition(record.topic(), record.partition());
                        OffsetAndMetadata offsetMetadata = new OffsetAndMetadata(record.offset() + 1);
                        consumer.commitSync(Collections.singletonMap(partition, offsetMetadata));
                        
                        logger.debug("✅ Committed offset {} for partition {}", record.offset() + 1, partition);
                        
                        if (messageCount >= maxMessages) {
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("❌ Error processing message at offset {}: {}", record.offset(), e.getMessage());
                    }
                }
            }
            
            logger.info("✅ Raw consumer processed {} messages", messageCount);
            
        } catch (Exception e) {
            logger.error("❌ Raw consumer failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process a user event message
     */
    private void processUserEvent(String message) {
        logger.debug("🔄 Processing user event: {}", message);
        
        // Simulate processing time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Here you would implement your business logic
        // For example: parse JSON, validate data, store in database, etc.
        
        // Example: Extract user_id from JSON-like message
        if (message.contains("user_id")) {
            String userId = extractUserId(message);
            logger.debug("👤 Processing event for user: {}", userId);
        }
    }
    
    /**
     * Process an individual message (raw consumer)
     */
    private void processMessage(ConsumerRecord<String, String> record) {
        logger.debug("🔄 Processing message: topic={}, partition={}, offset={}, key={}, value={}", 
            record.topic(), record.partition(), record.offset(), record.key(), record.value());
        
        // Simulate processing time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Business logic implementation would go here
    }
    
    /**
     * Extract user ID from a JSON-like message (simple implementation)
     */
    private String extractUserId(String message) {
        try {
            // Simple extraction for demo purposes
            int start = message.indexOf("\"user_id\":\"") + 11;
            int end = message.indexOf("\"", start);
            return message.substring(start, end);
        } catch (Exception e) {
            logger.warn("⚠️  Could not extract user_id from message: {}", message);
            return "unknown";
        }
    }
    
    /**
     * Demonstrate different consumer patterns
     */
    public void demonstrateConsumerPatterns() {
        logger.info("🎓 Starting Day 4 Consumer Demonstration");
        
        try {
            logger.info("1️⃣  Spring Kafka Listeners are already running automatically");
            logger.info("   - Check logs for messages received via @KafkaListener");
            
            logger.info("2️⃣  Demonstrating raw consumer (will consume 5 messages)");
            consumeMessagesRaw("user-events", "raw-training-group", 5);
            
            logger.info("3️⃣  Current message count processed by Spring listeners: {}", messageCount.get());
            
            logger.info("✅ Day 4 Consumer Demonstration completed");
            
        } catch (Exception e) {
            logger.error("❌ Day 4 Consumer Demonstration failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get current message processing statistics
     */
    public int getProcessedMessageCount() {
        return messageCount.get();
    }
    
    /**
     * Reset message counter
     */
    public void resetMessageCounter() {
        messageCount.set(0);
        logger.info("🔄 Message counter reset to 0");
    }
    
    /**
     * Demonstrate consumer group behavior by creating multiple consumers
     */
    public void demonstrateConsumerGroup(String topic) {
        logger.info("👥 Demonstrating consumer group behavior");
        
        // This would typically be done with multiple application instances
        // For demo purposes, we'll just log the concept
        logger.info("💡 Consumer Group Concepts:");
        logger.info("  - Multiple consumers in same group share partitions");
        logger.info("  - Each partition is consumed by only one consumer in the group");
        logger.info("  - If a consumer fails, partitions are rebalanced");
        logger.info("  - Different groups consume all messages independently");
        
        // Start a raw consumer to demonstrate
        consumeMessagesRaw(topic, "group-demo-" + System.currentTimeMillis(), 3);
    }
}
