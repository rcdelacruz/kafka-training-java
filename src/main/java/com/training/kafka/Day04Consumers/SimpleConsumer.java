package com.training.kafka.Day04Consumers;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Day 4: Simple Kafka Consumer
 * 
 * This class demonstrates basic Kafka consumer functionality.
 * It shows how to create, configure, and use a consumer to read messages.
 */
public class SimpleConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);
    
    private final KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;
    
    public SimpleConsumer(String bootstrapServers, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "simple-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Consumer behavior settings
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Manual commit for better control
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");
        
        this.consumer = new KafkaConsumer<>(props);
    }
    
    /**
     * Consume messages from a topic with automatic offset commit
     */
    public void consumeMessages(String topicName) {
        logger.info("Starting to consume messages from topic: {}", topicName);
        
        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(topicName));
        
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                if (records.isEmpty()) {
                    continue;
                }
                
                logger.info("Received {} messages", records.count());
                
                for (ConsumerRecord<String, String> record : records) {
                    processMessage(record);
                }
                
                // Commit offsets after processing all messages in this batch
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
            logger.info("Consumer closed");
        }
    }
    
    /**
     * Consume messages with manual offset management
     */
    public void consumeWithManualOffsetManagement(String topicName, int maxMessages) {
        logger.info("Starting to consume {} messages from topic: {}", maxMessages, topicName);
        
        consumer.subscribe(Collections.singletonList(topicName));
        
        int messageCount = 0;
        
        try {
            while (running && messageCount < maxMessages) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        processMessage(record);
                        messageCount++;
                        
                        // Commit offset for this specific record
                        TopicPartition partition = new TopicPartition(record.topic(), record.partition());
                        OffsetAndMetadata offsetMetadata = new OffsetAndMetadata(record.offset() + 1);
                        consumer.commitSync(Collections.singletonMap(partition, offsetMetadata));
                        
                        logger.debug("Committed offset {} for partition {}", record.offset() + 1, partition);
                        
                        if (messageCount >= maxMessages) {
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("Error processing message at offset {}: {}", record.offset(), e.getMessage());
                        // In real applications, you might want to implement retry logic or dead letter queues
                    }
                }
            }
        } finally {
            consumer.close();
            logger.info("Processed {} messages and closed consumer", messageCount);
        }
    }
    
    /**
     * Process an individual message
     */
    private void processMessage(ConsumerRecord<String, String> record) {
        logger.info("Processing message: topic={}, partition={}, offset={}, key={}, value={}", 
            record.topic(), record.partition(), record.offset(), record.key(), record.value());
        
        // Simulate processing time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Here you would implement your business logic
        // For example: parse JSON, validate data, store in database, etc.
    }
    
    /**
     * Demonstrate consumer group behavior
     */
    public static void demonstrateConsumerGroup(String bootstrapServers, String topicName) {
        logger.info("Demonstrating consumer group behavior...");
        
        // Create multiple consumers in the same group
        String groupId = "demo-consumer-group";
        
        // Consumer 1
        Thread consumer1Thread = new Thread(() -> {
            SimpleConsumer consumer1 = new SimpleConsumer(bootstrapServers, groupId);
            consumer1.consumeWithManualOffsetManagement(topicName, 5);
        }, "Consumer-1");
        
        // Consumer 2
        Thread consumer2Thread = new Thread(() -> {
            SimpleConsumer consumer2 = new SimpleConsumer(bootstrapServers, groupId);
            consumer2.consumeWithManualOffsetManagement(topicName, 5);
        }, "Consumer-2");
        
        // Start both consumers
        consumer1Thread.start();
        consumer2Thread.start();
        
        try {
            consumer1Thread.join();
            consumer2Thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for consumer threads");
        }
        
        logger.info("Consumer group demonstration completed");
    }
    
    /**
     * Stop the consumer
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topicName = "user-events";
        String groupId = "training-consumer-group";
        
        if (args.length > 0 && "group-demo".equals(args[0])) {
            // Demonstrate consumer group behavior
            demonstrateConsumerGroup(bootstrapServers, topicName);
        } else {
            // Simple consumer demonstration
            SimpleConsumer consumer = new SimpleConsumer(bootstrapServers, groupId);
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received");
                consumer.stop();
            }));
            
            // Start consuming messages
            consumer.consumeMessages(topicName);
        }
    }
}