package com.training.kafka.Day01Foundation;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BasicTopicOperations using TestContainers
 */
@Testcontainers
class BasicTopicOperationsTest {
    
    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();
    
    private BasicTopicOperations topicOperations;
    private AdminClient adminClient;
    
    @BeforeEach
    void setUp() {
        String bootstrapServers = kafka.getBootstrapServers();
        topicOperations = new BasicTopicOperations(bootstrapServers);
        
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminClient = AdminClient.create(props);
    }
    
    @AfterEach
    void tearDown() {
        if (topicOperations != null) {
            topicOperations.close();
        }
        if (adminClient != null) {
            adminClient.close();
        }
    }
    
    @Test
    void shouldCreateTopicSuccessfully() throws Exception {
        // Given
        String topicName = "test-topic-" + System.currentTimeMillis();
        int partitions = 3;
        short replicationFactor = 1;
        
        // When
        topicOperations.createTopic(topicName, partitions, replicationFactor);
        
        // Then
        TimeUnit.SECONDS.sleep(2); // Wait for topic creation
        Set<String> topics = adminClient.listTopics().names().get();
        assertTrue(topics.contains(topicName), "Topic should be created");
    }
    
    @Test
    void shouldListTopicsCorrectly() throws Exception {
        // Given
        String topicName1 = "list-test-topic-1-" + System.currentTimeMillis();
        String topicName2 = "list-test-topic-2-" + System.currentTimeMillis();
        
        topicOperations.createTopic(topicName1, 1, (short) 1);
        topicOperations.createTopic(topicName2, 1, (short) 1);
        
        TimeUnit.SECONDS.sleep(2);
        
        // When
        Set<String> topics = topicOperations.listTopics();
        
        // Then
        assertTrue(topics.contains(topicName1), "Should contain first topic");
        assertTrue(topics.contains(topicName2), "Should contain second topic");
        assertTrue(topics.size() >= 2, "Should have at least 2 topics");
    }
    
    @Test
    void shouldDescribeTopicCorrectly() throws Exception {
        // Given
        String topicName = "describe-test-topic-" + System.currentTimeMillis();
        int partitions = 2;
        
        topicOperations.createTopic(topicName, partitions, (short) 1);
        TimeUnit.SECONDS.sleep(2);
        
        // When & Then (verificação via logs)
        assertDoesNotThrow(() -> topicOperations.describeTopic(topicName));
    }
    
    @Test
    void shouldHandleTopicAlreadyExists() {
        // Given
        String topicName = "duplicate-topic-" + System.currentTimeMillis();
        
        // When & Then
        assertDoesNotThrow(() -> {
            topicOperations.createTopic(topicName, 1, (short) 1);
            topicOperations.createTopic(topicName, 1, (short) 1); // Should not throw
        });
    }
    
    @Test
    void shouldConnectToKafkaCluster() {
        // When & Then
        assertDoesNotThrow(() -> {
            Set<String> topics = topicOperations.listTopics();
            assertNotNull(topics, "Topics list should not be null");
        });
    }
}