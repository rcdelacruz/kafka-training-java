package com.training.kafka.progression;

import com.training.kafka.Day01Foundation.BasicTopicOperations;
import com.training.kafka.Day03Producers.SimpleProducer;
import com.training.kafka.Day04Consumers.SimpleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Progressive Learning Example: Day 1 → Day 2 → Day 3 → Day 4
 * 
 * This class demonstrates the learning progression through the course:
 * 1. Day 1: Create topics with AdminClient
 * 2. Day 2: Data flow concepts (partitioning, keys)
 * 3. Day 3: Produce messages with various patterns
 * 4. Day 4: Consume messages with different strategies
 */
public class ProgressiveLearningExample {
    private static final Logger logger = LoggerFactory.getLogger(ProgressiveLearningExample.class);
    
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC_NAME = "progressive-learning-topic";
    private static final String CONSUMER_GROUP = "progressive-learning-group";
    
    public static void main(String[] args) {
        logger.info("🚀 Starting Progressive Learning Example: Day 1 → Day 4");
        logger.info("This example shows the complete journey from topic creation to message consumption");
        
        try {
            // Day 1: Foundation - Create topic with AdminClient
            demonstrateDay1Foundation();
            
            Thread.sleep(2000); // Give time for topic creation
            
            // Day 2: Data Flow - Understanding partitioning and keys (conceptual)
            demonstrateDay2DataFlow();
            
            // Day 3: Producers - Send messages with different patterns
            demonstrateDay3Producers();
            
            Thread.sleep(2000); // Give time for messages to be produced
            
            // Day 4: Consumers - Read and process messages
            demonstrateDay4Consumers();
            
            logger.info("✅ Progressive Learning Example Completed!");
            logger.info("You've seen the complete Kafka workflow from creation to consumption");
            
        } catch (Exception e) {
            logger.error("❌ Error in progressive learning example", e);
        }
    }
    
    /**
     * Day 1: Foundation - Topic Management with AdminClient
     */
    private static void demonstrateDay1Foundation() {
        logger.info("\n=== Day 1: Foundation - Topic Creation ===");
        logger.info("Creating topic using AdminClient...");
        
        BasicTopicOperations topicOps = new BasicTopicOperations(BOOTSTRAP_SERVERS);
        
        try {
            // Create topic with specific configuration
            topicOps.createTopic(TOPIC_NAME, 3, (short) 1);
            
            // List topics to verify creation
            topicOps.listTopics();
            
            // Describe the topic
            topicOps.describeTopic(TOPIC_NAME);
            
            logger.info("✅ Day 1 Complete: Topic '{}' created with 3 partitions", TOPIC_NAME);
            
        } finally {
            topicOps.close();
        }
    }
    
    /**
     * Day 2: Data Flow - Understanding partitioning and message routing
     */
    private static void demonstrateDay2DataFlow() {
        logger.info("\n=== Day 2: Data Flow - Understanding Message Routing ===");
        logger.info("Key Concepts:");
        logger.info("• Messages without keys → Round-robin distribution");
        logger.info("• Messages with same key → Same partition (ordering guarantee)");
        logger.info("• Different keys → May go to different partitions");
        logger.info("• Consumer groups → Load balancing across consumers");
        logger.info("✅ Day 2 Concepts: Ready to implement producers and consumers");
    }
    
    /**
     * Day 3: Producers - Send messages with different patterns
     */
    private static void demonstrateDay3Producers() {
        logger.info("\n=== Day 3: Producers - Sending Messages ===");
        logger.info("Sending messages with different patterns...");
        
        SimpleProducer producer = new SimpleProducer(BOOTSTRAP_SERVERS, TOPIC_NAME);
        
        try {
            // Pattern 1: Messages without keys (round-robin)
            logger.info("📤 Sending messages without keys (round-robin distribution):");
            producer.sendMessageAsync(null, "Message 1 - no key");
            producer.sendMessageAsync(null, "Message 2 - no key");
            producer.sendMessageAsync(null, "Message 3 - no key");
            
            // Pattern 2: Messages with keys (same key → same partition)
            logger.info("📤 Sending messages with keys (key-based routing):");
            producer.sendMessageAsync("user1", "User 1 login event");
            producer.sendMessageAsync("user1", "User 1 purchase event");
            producer.sendMessageAsync("user2", "User 2 login event");
            producer.sendMessageAsync("user1", "User 1 logout event");
            
            // Pattern 3: Batch messages for efficiency
            logger.info("📤 Sending batch messages:");
            producer.sendBatchMessages(5);
            
            logger.info("✅ Day 3 Complete: Multiple message patterns sent successfully");
            
        } finally {
            producer.close();
        }
    }
    
    /**
     * Day 4: Consumers - Read and process messages
     */
    private static void demonstrateDay4Consumers() {
        logger.info("\n=== Day 4: Consumers - Reading Messages ===");
        logger.info("Starting consumer to read all the messages we sent...");
        
        // Create consumer and process messages
        SimpleConsumer consumer = new SimpleConsumer(BOOTSTRAP_SERVERS, CONSUMER_GROUP);
        
        // Start consumer in a separate thread
        Thread consumerThread = new Thread(() -> {
            try {
                consumer.consumeWithManualOffsetManagement(TOPIC_NAME, 15); // Read ~15 messages
            } catch (Exception e) {
                logger.error("Error in consumer", e);
            }
        });
        
        consumerThread.start();
        
        try {
            // Wait for consumer to finish
            consumerThread.join(30000); // Wait max 30 seconds
            
            if (consumerThread.isAlive()) {
                logger.info("⏰ Consumer still running - stopping for demo purposes");
                consumerThread.interrupt();
            }
            
            logger.info("✅ Day 4 Complete: Messages consumed and processed");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Consumer interrupted", e);
        }
    }
    
    /**
     * Summary of what each day teaches
     */
    public static void printLearningProgression() {
        logger.info("\n🎓 KAFKA LEARNING PROGRESSION SUMMARY:");
        logger.info("");
        logger.info("Day 1: Foundation");
        logger.info("  ✓ AdminClient for topic management");
        logger.info("  ✓ Understanding Kafka architecture");
        logger.info("  ✓ Topic, partition, and replica concepts");
        logger.info("");
        logger.info("Day 2: Data Flow");
        logger.info("  ✓ Producer and consumer semantics");
        logger.info("  ✓ Partitioning strategies (round-robin vs key-based)");
        logger.info("  ✓ Offset management and consumer groups");
        logger.info("  ✓ Message ordering guarantees");
        logger.info("");
        logger.info("Day 3: Producers");
        logger.info("  ✓ Synchronous vs asynchronous sending");
        logger.info("  ✓ Error handling and retry patterns");
        logger.info("  ✓ Batching and performance optimization");
        logger.info("  ✓ Delivery guarantees (at-most, at-least, exactly-once)");
        logger.info("");
        logger.info("Day 4: Consumers");
        logger.info("  ✓ Consumer groups and load balancing");
        logger.info("  ✓ Manual vs automatic offset management");
        logger.info("  ✓ Partition assignment and rebalancing");
        logger.info("  ✓ Error handling and recovery patterns");
        logger.info("");
        logger.info("Next Steps: Stream Processing, Schema Management, Connect, Security...");
    }
}
