package com.training.kafka.Day02DataFlow;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * Day 2: Consumer Data Flow Demo
 * 
 * Demonstrates consumer concepts:
 * - Offset management strategies
 * - Consumer group behavior
 * - Partition assignment
 * - Message ordering within partitions
 */
public class ConsumerDataFlowDemo {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerDataFlowDemo.class);
    
    private final String bootstrapServers;
    private final String topicName;
    
    public ConsumerDataFlowDemo(String bootstrapServers, String topicName) {
        this.bootstrapServers = bootstrapServers;
        this.topicName = topicName;
    }
    
    /**
     * Demonstrate auto-commit vs manual commit
     */
    public void demonstrateOffsetManagement() {
        logger.info("=== Offset Management Demo ===");
        
        // Auto-commit consumer
        logger.info("1. Auto-commit consumer (enable.auto.commit=true):");
        KafkaConsumer<String, String> autoCommitConsumer = createConsumer("auto-commit-group", true);
        
        try {
            autoCommitConsumer.subscribe(Collections.singletonList(topicName));
            
            ConsumerRecords<String, String> records = autoCommitConsumer.poll(Duration.ofSeconds(5));
            logger.info("   Auto-commit consumer received {} records", records.count());
            
            for (ConsumerRecord<String, String> record : records) {
                logger.info("   Auto-commit: Partition {}, Offset {}, Key: {}, Value: {}", 
                    record.partition(), record.offset(), record.key(), record.value());
                
                if (records.count() >= 3) break; // Limit output
            }
            
            logger.info("   → Offsets will be committed automatically every 5 seconds");
            
        } finally {
            autoCommitConsumer.close();
        }
        
        // Manual commit consumer
        logger.info("2. Manual-commit consumer (enable.auto.commit=false):");
        KafkaConsumer<String, String> manualCommitConsumer = createConsumer("manual-commit-group", false);
        
        try {
            manualCommitConsumer.subscribe(Collections.singletonList(topicName));
            
            ConsumerRecords<String, String> records = manualCommitConsumer.poll(Duration.ofSeconds(5));
            logger.info("   Manual-commit consumer received {} records", records.count());
            
            int processedCount = 0;
            for (ConsumerRecord<String, String> record : records) {
                logger.info("   Manual-commit: Partition {}, Offset {}, Key: {}, Value: {}", 
                    record.partition(), record.offset(), record.key(), record.value());
                
                processedCount++;
                
                // Simulate processing and commit after each message
                try {
                    manualCommitConsumer.commitSync();
                    logger.info("   → Manually committed offset {} for partition {}", 
                        record.offset() + 1, record.partition());
                } catch (Exception e) {
                    logger.error("   → Failed to commit offset: {}", e.getMessage());
                }
                
                if (processedCount >= 3) break; // Limit output
            }
            
        } finally {
            manualCommitConsumer.close();
        }
        
        logger.info("Offset management demo complete.");
        logger.info("");
    }
    
    /**
     * Demonstrate consumer groups and partition assignment
     */
    public void demonstrateConsumerGroups() {
        logger.info("=== Consumer Groups Demo ===");
        logger.info("Starting multiple consumers in the same group...");
        
        String groupId = "consumer-group-demo";
        
        // Create two consumers in the same group
        Thread consumer1Thread = new Thread(() -> {
            KafkaConsumer<String, String> consumer1 = createConsumer(groupId + "-1", true);
            runConsumer(consumer1, "Consumer-1", 10);
        }, "Consumer-1-Thread");
        
        Thread consumer2Thread = new Thread(() -> {
            KafkaConsumer<String, String> consumer2 = createConsumer(groupId + "-2", true);
            runConsumer(consumer2, "Consumer-2", 10);
        }, "Consumer-2-Thread");
        
        // Start both consumers
        consumer1Thread.start();
        
        try {
            Thread.sleep(2000); // Let first consumer get partitions
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        consumer2Thread.start();
        
        try {
            consumer1Thread.join(15000); // Wait max 15 seconds
            consumer2Thread.join(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Consumer group demo interrupted");
        }
        
        logger.info("Consumer groups demo complete.");
        logger.info("");
    }
    
    /**
     * Demonstrate message ordering within partitions
     */
    public void demonstrateMessageOrdering() {
        logger.info("=== Message Ordering Demo ===");
        logger.info("Consuming messages to show partition-level ordering...");
        
        KafkaConsumer<String, String> consumer = createConsumer("ordering-demo-group", false);
        
        try {
            consumer.subscribe(Collections.singletonList(topicName));
            
            // Consume messages and show ordering
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            
            logger.info("Received {} records - showing ordering by partition:", records.count());
            
            // Group by partition to show ordering
            for (TopicPartition partition : records.partitions()) {
                logger.info("--- Partition {} ---", partition.partition());
                
                var partitionRecords = records.records(partition);
                for (ConsumerRecord<String, String> record : partitionRecords) {
                    logger.info("  Offset {}: Key='{}', Value='{}'", 
                        record.offset(), record.key(), record.value());
                }
            }
            
            logger.info("Notice: Messages within each partition are ordered by offset");
            logger.info("        Messages across partitions may not be globally ordered");
            
        } finally {
            consumer.close();
        }
        
        logger.info("Message ordering demo complete.");
        logger.info("");
    }
    
    /**
     * Demonstrate seeking to specific offsets
     */
    public void demonstrateOffsetSeeking() {
        logger.info("=== Offset Seeking Demo ===");
        
        KafkaConsumer<String, String> consumer = createConsumer("seeking-demo-group", false);
        
        try {
            consumer.subscribe(Collections.singletonList(topicName));
            
            // Initial poll to get partition assignment
            consumer.poll(Duration.ofMillis(100));
            Set<TopicPartition> assignment = consumer.assignment();
            
            if (assignment.isEmpty()) {
                logger.warn("No partitions assigned. Make sure topic exists and has data.");
                return;
            }
            
            logger.info("Assigned partitions: {}", assignment);
            
            // Demonstrate seeking to beginning
            logger.info("1. Seeking to beginning of all partitions:");
            consumer.seekToBeginning(assignment);
            
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            logger.info("   Read {} records from beginning", records.count());
            
            // Show first few records
            int count = 0;
            for (ConsumerRecord<String, String> record : records) {
                logger.info("   Beginning: Partition {}, Offset {}, Value: {}", 
                    record.partition(), record.offset(), record.value());
                if (++count >= 3) break;
            }
            
            // Demonstrate seeking to end
            logger.info("2. Seeking to end of all partitions:");
            consumer.seekToEnd(assignment);
            
            records = consumer.poll(Duration.ofSeconds(5));
            logger.info("   Read {} records from end (should be 0 or very few)", records.count());
            
            // Demonstrate seeking to specific offset
            if (!assignment.isEmpty()) {
                TopicPartition firstPartition = assignment.iterator().next();
                logger.info("3. Seeking to offset 0 on partition {}:", firstPartition.partition());
                consumer.seek(firstPartition, 0);
                
                records = consumer.poll(Duration.ofSeconds(5));
                for (ConsumerRecord<String, String> record : records) {
                    if (record.partition() == firstPartition.partition()) {
                        logger.info("   Specific seek: Partition {}, Offset {}, Value: {}", 
                            record.partition(), record.offset(), record.value());
                        break; // Just show first one
                    }
                }
            }
            
        } finally {
            consumer.close();
        }
        
        logger.info("Offset seeking demo complete.");
        logger.info("");
    }
    
    /**
     * Create a consumer with specific configuration
     */
    private KafkaConsumer<String, String> createConsumer(String groupId, boolean autoCommit) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        
        if (autoCommit) {
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
        }
        
        // Shorter timeouts for demo
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        
        return new KafkaConsumer<>(props);
    }
    
    /**
     * Run a consumer for a specific duration
     */
    private void runConsumer(KafkaConsumer<String, String> consumer, String consumerName, int maxMessages) {
        try {
            consumer.subscribe(Collections.singletonList(topicName));
            logger.info("{} subscribed to topic: {}", consumerName, topicName);
            
            int messageCount = 0;
            long startTime = System.currentTimeMillis();
            
            while (messageCount < maxMessages && (System.currentTimeMillis() - startTime) < 15000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("{}: Partition {}, Offset {}, Key: {}, Value: {}", 
                        consumerName, record.partition(), record.offset(), record.key(), record.value());
                    
                    messageCount++;
                    if (messageCount >= maxMessages) break;
                }
                
                if (records.isEmpty()) {
                    logger.info("{}: No messages received (waiting...)", consumerName);
                }
            }
            
            logger.info("{} processed {} messages", consumerName, messageCount);
            
        } catch (Exception e) {
            logger.error("Error in {}: {}", consumerName, e.getMessage());
        } finally {
            consumer.close();
            logger.info("{} closed", consumerName);
        }
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topicName = "data-flow-demo";
        
        logger.info("Starting Day 2: Consumer Data Flow Demonstrations");
        logger.info("Topic: {}", topicName);
        logger.info("Make sure you've run the InteractiveProducer first to generate data!");
        logger.info("");
        
        ConsumerDataFlowDemo demo = new ConsumerDataFlowDemo(bootstrapServers, topicName);
        
        try {
            // Run all demonstrations
            demo.demonstrateOffsetManagement();
            
            Thread.sleep(3000); // Pause between demos
            
            demo.demonstrateMessageOrdering();
            
            Thread.sleep(3000);
            
            demo.demonstrateOffsetSeeking();
            
            Thread.sleep(3000);
            
            demo.demonstrateConsumerGroups();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Demo interrupted");
        } catch (Exception e) {
            logger.error("Error in demo", e);
        }
        
        logger.info("All Day 2 consumer demonstrations complete!");
    }
}