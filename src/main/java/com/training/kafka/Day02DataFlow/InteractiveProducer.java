package com.training.kafka.Day02DataFlow;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Scanner;

/**
 * Day 2: Interactive Producer Demo
 * 
 * Demonstrates data flow concepts with interactive message production.
 * Shows partitioning, key-based routing, and producer patterns.
 */
public class InteractiveProducer {
    private static final Logger logger = LoggerFactory.getLogger(InteractiveProducer.class);
    
    private final KafkaProducer<String, String> producer;
    private final String topicName;
    
    public InteractiveProducer(String bootstrapServers, String topicName) {
        this.topicName = topicName;
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "interactive-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Configuration for learning about data flow
        props.put(ProducerConfig.ACKS_CONFIG, "1"); // Fast acknowledgment
        props.put(ProducerConfig.LINGER_MS_CONFIG, "0"); // Send immediately
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "1"); // No batching for demo
        
        this.producer = new KafkaProducer<>(props);
        logger.info("Interactive producer created for topic: {}", topicName);
    }
    
    /**
     * Demonstrate round-robin partitioning (no key)
     */
    public void demonstrateRoundRobinPartitioning() {
        logger.info("=== Round-Robin Partitioning Demo ===");
        logger.info("Sending messages WITHOUT keys - watch partition assignment");
        
        for (int i = 1; i <= 5; i++) {
            String message = "Round-robin message " + i;
            
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, message);
            
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Message '{}' → Partition: {}, Offset: {}", 
                        message, metadata.partition(), metadata.offset());
                } else {
                    logger.error("Error sending message: {}", exception.getMessage());
                }
            });
        }
        
        producer.flush();
        logger.info("Round-robin demo complete. Notice how messages are distributed across partitions.");
        logger.info("");
    }
    
    /**
     * Demonstrate key-based partitioning
     */
    public void demonstrateKeyBasedPartitioning() {
        logger.info("=== Key-Based Partitioning Demo ===");
        logger.info("Sending messages WITH keys - same key goes to same partition");
        
        String[] users = {"alice", "bob", "charlie", "alice", "bob", "charlie"};
        
        for (int i = 0; i < users.length; i++) {
            String key = users[i];
            String message = "Message " + (i + 1) + " from user " + key;
            
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, message);
            
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Key: '{}' → Message: '{}' → Partition: {}, Offset: {}", 
                        key, message, metadata.partition(), metadata.offset());
                } else {
                    logger.error("Error sending message: {}", exception.getMessage());
                }
            });
        }
        
        producer.flush();
        logger.info("Key-based demo complete. Notice how same keys go to same partitions.");
        logger.info("");
    }
    
    /**
     * Interactive message sending
     */
    public void interactiveMode() {
        logger.info("=== Interactive Mode ===");
        logger.info("Enter messages to send. Format: [key:]message");
        logger.info("Examples:");
        logger.info("  'hello world' (no key)");
        logger.info("  'user1:login event' (with key)");
        logger.info("Type 'quit' to exit");
        logger.info("");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("Enter message: ");
            String input = scanner.nextLine().trim();
            
            if ("quit".equalsIgnoreCase(input)) {
                break;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            String key = null;
            String message = input;
            
            // Check if input has key:value format
            if (input.contains(":")) {
                String[] parts = input.split(":", 2);
                if (parts.length == 2 && !parts[0].isEmpty()) {
                    key = parts[0];
                    message = parts[1];
                }
            }
            
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, message);
            
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    String keyInfo = key != null ? "Key: '" + key + "' → " : "No key → ";
                    logger.info("{}Message: '{}' → Partition: {}, Offset: {}", 
                        keyInfo, message, metadata.partition(), metadata.offset());
                } else {
                    logger.error("Error sending message: {}", exception.getMessage());
                }
            });
            
            producer.flush();
        }
        
        scanner.close();
        logger.info("Interactive mode ended.");
    }
    
    /**
     * Demonstrate producer delivery semantics
     */
    public void demonstrateDeliverySemantics() {
        logger.info("=== Delivery Semantics Demo ===");
        
        // At-most-once configuration (fast but may lose messages)
        logger.info("1. At-most-once semantics (acks=0, retries=0):");
        Properties atMostOnceProps = new Properties();
        atMostOnceProps.putAll(producer.configs());
        atMostOnceProps.put(ProducerConfig.ACKS_CONFIG, "0");
        atMostOnceProps.put(ProducerConfig.RETRIES_CONFIG, "0");
        
        KafkaProducer<String, String> atMostOnceProducer = new KafkaProducer<>(atMostOnceProps);
        
        atMostOnceProducer.send(new ProducerRecord<>(topicName, "at-most-once", "Fast message, may be lost"));
        atMostOnceProducer.flush();
        atMostOnceProducer.close();
        logger.info("   → Message sent with no guarantees (fastest)");
        
        // At-least-once configuration (reliable but may duplicate)
        logger.info("2. At-least-once semantics (acks=all, retries=3):");
        Properties atLeastOnceProps = new Properties();
        atLeastOnceProps.putAll(producer.configs());
        atLeastOnceProps.put(ProducerConfig.ACKS_CONFIG, "all");
        atLeastOnceProps.put(ProducerConfig.RETRIES_CONFIG, "3");
        
        KafkaProducer<String, String> atLeastOnceProducer = new KafkaProducer<>(atLeastOnceProps);
        
        atLeastOnceProducer.send(new ProducerRecord<>(topicName, "at-least-once", "Reliable message, no loss"));
        atLeastOnceProducer.flush();
        atLeastOnceProducer.close();
        logger.info("   → Message sent with delivery guarantee (reliable)");
        
        logger.info("Delivery semantics demo complete.");
        logger.info("");
    }
    
    public void close() {
        producer.close();
        logger.info("Interactive producer closed");
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topicName = "data-flow-demo";
        
        // Create topic first if it doesn't exist
        logger.info("Starting Day 2: Data Flow Demonstrations");
        logger.info("Topic: {}", topicName);
        logger.info("Make sure the topic exists: confluent local kafka topic create {}", topicName);
        logger.info("");
        
        InteractiveProducer demo = new InteractiveProducer(bootstrapServers, topicName);
        
        try {
            // Run all demonstrations
            demo.demonstrateRoundRobinPartitioning();
            
            Thread.sleep(2000); // Pause between demos
            
            demo.demonstrateKeyBasedPartitioning();
            
            Thread.sleep(2000);
            
            demo.demonstrateDeliverySemantics();
            
            Thread.sleep(2000);
            
            // Interactive mode
            demo.interactiveMode();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Demo interrupted");
        } catch (Exception e) {
            logger.error("Error in demo", e);
        } finally {
            demo.close();
        }
    }
}