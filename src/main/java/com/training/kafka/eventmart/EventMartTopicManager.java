package com.training.kafka.eventmart;

import com.training.kafka.common.KafkaConfig;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * EventMart Topic Manager
 *
 * Day 1 Deliverable: Creates and manages all EventMart topics with proper configuration
 *
 * This class demonstrates:
 * - Topic creation with specific configurations
 * - Partitioning strategies for different use cases
 * - Retention and cleanup policies
 * - AdminClient operations
 */
public class EventMartTopicManager {
    private static final Logger logger = LoggerFactory.getLogger(EventMartTopicManager.class);

    private final AdminClient adminClient;

    // EventMart Topic Definitions
    private static final Map<String, TopicDefinition> EVENTMART_TOPICS = Map.of(
        "eventmart-users", new TopicDefinition(3, (short) 1,
            Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT,
                   TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, "0.1")),

        "eventmart-products", new TopicDefinition(5, (short) 1,
            Map.of(TopicConfig.RETENTION_MS_CONFIG, "604800000", // 7 days
                   TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")),

        "eventmart-orders", new TopicDefinition(10, (short) 1,
            Map.of(TopicConfig.RETENTION_MS_CONFIG, "2592000000", // 30 days
                   TopicConfig.SEGMENT_MS_CONFIG, "86400000")), // 1 day segments

        "eventmart-payments", new TopicDefinition(3, (short) 1,
            Map.of(TopicConfig.RETENTION_MS_CONFIG, "7776000000", // 90 days
                   TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1")),

        "eventmart-notifications", new TopicDefinition(2, (short) 1,
            Map.of(TopicConfig.RETENTION_MS_CONFIG, "86400000", // 1 day
                   TopicConfig.DELETE_RETENTION_MS_CONFIG, "3600000")), // 1 hour

        "eventmart-analytics", new TopicDefinition(1, (short) 1,
            Map.of(TopicConfig.RETENTION_MS_CONFIG, "3600000", // 1 hour
                   TopicConfig.SEGMENT_MS_CONFIG, "600000")), // 10 minute segments

        "eventmart-audit", new TopicDefinition(1, (short) 1,
            Map.of(TopicConfig.RETENTION_MS_CONFIG, "31536000000", // 1 year
                   TopicConfig.COMPRESSION_TYPE_CONFIG, "lz4"))
    );

    public EventMartTopicManager(String bootstrapServers) {
        Properties props = KafkaConfig.createAdminProperties(bootstrapServers);
        this.adminClient = AdminClient.create(props);
    }

    /**
     * Day 1 Main Method: Create all EventMart topics
     */
    public void createEventMartTopics() {
        logger.info("=== Creating EventMart Topic Architecture ===");

        try {
            // Check existing topics
            Set<String> existingTopics = listExistingTopics();
            logger.info("Found {} existing topics", existingTopics.size());

            // Create new topics
            List<NewTopic> topicsToCreate = new ArrayList<>();

            for (Map.Entry<String, TopicDefinition> entry : EVENTMART_TOPICS.entrySet()) {
                String topicName = entry.getKey();
                TopicDefinition definition = entry.getValue();

                if (!existingTopics.contains(topicName)) {
                    NewTopic newTopic = new NewTopic(topicName, definition.partitions, definition.replicationFactor)
                            .configs(definition.configs);
                    topicsToCreate.add(newTopic);
                    logger.info("Preparing to create topic: {} with {} partitions",
                               topicName, definition.partitions);
                } else {
                    logger.info("Topic already exists: {}", topicName);
                }
            }

            if (!topicsToCreate.isEmpty()) {
                CreateTopicsResult result = adminClient.createTopics(topicsToCreate);
                result.all().get(); // Wait for completion
                logger.info("Successfully created {} EventMart topics", topicsToCreate.size());
            } else {
                logger.info("All EventMart topics already exist");
            }

            // Verify and describe all topics
            describeEventMartTopics();

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating EventMart topics", e);
            throw new RuntimeException("Failed to create EventMart topics", e);
        }
    }

    /**
     * List all existing topics
     */
    private Set<String> listExistingTopics() throws InterruptedException, ExecutionException {
        ListTopicsResult result = adminClient.listTopics();
        return result.names().get();
    }

    /**
     * Describe all EventMart topics
     */
    public void describeEventMartTopics() {
        logger.info("=== EventMart Topic Configuration ===");

        try {
            DescribeTopicsResult result = adminClient.describeTopics(EVENTMART_TOPICS.keySet());
            Map<String, TopicDescription> descriptions = result.allTopicNames().get();

            for (Map.Entry<String, TopicDescription> entry : descriptions.entrySet()) {
                String topicName = entry.getKey();
                TopicDescription description = entry.getValue();

                logger.info("Topic: {}", topicName);
                logger.info("  Partitions: {}", description.partitions().size());
                logger.info("  Replication Factor: {}",
                           description.partitions().get(0).replicas().size());

                // Get topic configuration
                describeTopicConfig(topicName);
                logger.info(""); // Empty line for readability
            }

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error describing EventMart topics", e);
        }
    }

    /**
     * Describe topic configuration
     */
    private void describeTopicConfig(String topicName) {
        try {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
            DescribeConfigsResult result = adminClient.describeConfigs(Collections.singletonList(resource));
            Config config = result.all().get().get(resource);

            // Show key configurations
            for (ConfigEntry entry : config.entries()) {
                if (!entry.isDefault() && entry.value() != null) {
                    logger.info("  {}: {}", entry.name(), entry.value());
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error describing config for topic: {}", topicName, e);
        }
    }

    /**
     * Delete all EventMart topics (for cleanup)
     */
    public void deleteEventMartTopics() {
        logger.info("=== Deleting EventMart Topics ===");

        try {
            DeleteTopicsResult result = adminClient.deleteTopics(EVENTMART_TOPICS.keySet());
            result.all().get();
            logger.info("Successfully deleted all EventMart topics");

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error deleting EventMart topics", e);
        }
    }

    public void close() {
        adminClient.close();
    }

    /**
     * Topic Definition Helper Class
     */
    private static class TopicDefinition {
        final int partitions;
        final short replicationFactor;
        final Map<String, String> configs;

        TopicDefinition(int partitions, short replicationFactor, Map<String, String> configs) {
            this.partitions = partitions;
            this.replicationFactor = replicationFactor;
            this.configs = configs;
        }
    }

    /**
     * Main method for Day 1 deliverable
     */
    public static void main(String[] args) {
        String bootstrapServers = KafkaConfig.getBootstrapServers();

        EventMartTopicManager manager = new EventMartTopicManager(bootstrapServers);

        try {
            // Day 1 Deliverable: Create EventMart topic architecture
            manager.createEventMartTopics();

            logger.info("=== Day 1 Deliverable Complete ===");
            logger.info("EventMart topic architecture is ready!");
            logger.info("Next: Design message schemas and data flow (Day 2)");

        } finally {
            manager.close();
        }
    }
}
