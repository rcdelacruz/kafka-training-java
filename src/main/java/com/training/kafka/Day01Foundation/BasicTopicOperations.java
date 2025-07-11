package com.training.kafka.Day01Foundation;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Day 1: Basic Topic Operations
 * 
 * This class demonstrates basic Kafka topic operations using the AdminClient.
 * It shows how to create, list, describe, and delete topics programmatically.
 */
public class BasicTopicOperations {
    private static final Logger logger = LoggerFactory.getLogger(BasicTopicOperations.class);
    
    private final AdminClient adminClient;
    
    public BasicTopicOperations(String bootstrapServers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "basic-topic-operations");
        
        this.adminClient = AdminClient.create(props);
    }
    
    /**
     * Create a new topic with specified configuration
     */
    public void createTopic(String topicName, int numPartitions, short replicationFactor) {
        try {
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
            
            // Optional: Set topic-specific configurations
            Map<String, String> configs = new HashMap<>();
            configs.put("retention.ms", "86400000"); // 24 hours
            configs.put("compression.type", "snappy");
            newTopic.configs(configs);
            
            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
            
            // Wait for creation to complete
            result.all().get();
            logger.info("Topic '{}' created successfully with {} partitions", topicName, numPartitions);
            
        } catch (ExecutionException e) {
            if (e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException) {
                logger.warn("Topic '{}' already exists", topicName);
            } else {
                logger.error("Failed to create topic '{}': {}", topicName, e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Operation interrupted while creating topic '{}'", topicName);
        }
    }
    
    /**
     * List all topics in the cluster
     */
    public Set<String> listTopics() {
        try {
            ListTopicsResult result = adminClient.listTopics();
            Set<String> topics = result.names().get();
            
            logger.info("Found {} topics:", topics.size());
            topics.forEach(topic -> logger.info("  - {}", topic));
            
            return topics;
            
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to list topics: {}", e.getMessage());
            return Collections.emptySet();
        }
    }
    
    /**
     * Describe a specific topic
     */
    public void describeTopic(String topicName) {
        try {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
            TopicDescription description = result.topicNameValues().get(topicName).get();
            
            logger.info("Topic Description for '{}':", topicName);
            logger.info("  Name: {}", description.name());
            logger.info("  Internal: {}", description.isInternal());
            logger.info("  Partitions: {}", description.partitions().size());
            
            description.partitions().forEach(partition -> {
                logger.info("    Partition {}: Leader={}, Replicas={}, ISR={}",
                    partition.partition(),
                    partition.leader().id(),
                    partition.replicas().size(),
                    partition.isr().size());
            });
            
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to describe topic '{}': {}", topicName, e.getMessage());
        }
    }
    
    /**
     * Close the AdminClient
     */
    public void close() {
        adminClient.close();
        logger.info("AdminClient closed");
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        
        BasicTopicOperations topicOps = new BasicTopicOperations(bootstrapServers);
        
        try {
            // Create demonstration topics
            topicOps.createTopic("demo-topic-1", 3, (short) 1);
            topicOps.createTopic("demo-topic-2", 6, (short) 1);
            
            // List all topics
            topicOps.listTopics();
            
            // Describe a topic
            topicOps.describeTopic("demo-topic-1");
            
        } finally {
            topicOps.close();
        }
    }
}