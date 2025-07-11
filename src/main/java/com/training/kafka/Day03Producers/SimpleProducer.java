package com.training.kafka.Day03Producers;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Day 3: Simple Kafka Producer
 * 
 * This class demonstrates basic Kafka producer functionality.
 * It shows how to create, configure, and use a producer to send messages.
 */
public class SimpleProducer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);
    
    private final KafkaProducer<String, String> producer;
    private final String topicName;
    
    public SimpleProducer(String bootstrapServers, String topicName) {
        this.topicName = topicName;
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "simple-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Producer reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        
        // Performance settings
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Batch messages for 10ms
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batch size
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        this.producer = new KafkaProducer<>(props);
    }
    
    /**
     * Send a single message synchronously
     */
    public void sendMessageSync(String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, message);
        
        try {
            RecordMetadata metadata = producer.send(record).get();
            logger.info("Message sent successfully: topic={}, partition={}, offset={}, key={}", 
                metadata.topic(), metadata.partition(), metadata.offset(), key);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to send message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send a message asynchronously with callback
     */
    public void sendMessageAsync(String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, message);
        
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                logger.info("Message sent async: topic={}, partition={}, offset={}, key={}", 
                    metadata.topic(), metadata.partition(), metadata.offset(), key);
            } else {
                logger.error("Failed to send message async: {}", exception.getMessage(), exception);
            }
        });
    }
    
    /**
     * Send multiple messages in batch
     */
    public void sendBatchMessages(int count) {
        logger.info("Sending {} messages...", count);
        
        for (int i = 0; i < count; i++) {
            String key = "user-" + (i % 5); // 5 different users for partitioning
            String message = String.format("{\"user_id\":\"%s\", \"action\":\"login\", \"timestamp\":\"%s\", \"session_id\":\"%d\"}", 
                key, LocalDateTime.now(), System.currentTimeMillis() + i);
            
            sendMessageAsync(key, message);
        }
        
        // Flush to ensure all messages are sent
        producer.flush();
        logger.info("All {} messages sent successfully", count);
    }
    
    /**
     * Close the producer
     */
    public void close() {
        producer.close();
        logger.info("Producer closed");
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topicName = "user-events";
        
        SimpleProducer producer = new SimpleProducer(bootstrapServers, topicName);
        
        try {
            // Send single messages
            producer.sendMessageSync("user1", "User1 logged in");
            producer.sendMessageAsync("user2", "User2 performed action");
            
            // Send batch messages
            producer.sendBatchMessages(10);
            
        } finally {
            producer.close();
        }
    }
}