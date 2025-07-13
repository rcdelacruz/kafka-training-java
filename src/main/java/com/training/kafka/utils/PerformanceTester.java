package com.training.kafka.utils;

import com.training.kafka.common.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Performance Testing Utility for Kafka Training
 *
 * Provides simple performance testing capabilities to help trainees
 * understand Kafka performance characteristics:
 * - Producer throughput testing
 * - Consumer throughput testing
 * - Latency measurements
 * - Basic performance metrics
 */
public class PerformanceTester {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTester.class);

    /**
     * Test producer performance
     */
    public static class ProducerPerformanceTest {
        private final KafkaProducer<String, String> producer;
        private final String topicName;

        public ProducerPerformanceTest(String bootstrapServers, String topicName) {
            this.topicName = topicName;
            Properties props = KafkaConfig.createProducerProperties(bootstrapServers);
            this.producer = new KafkaProducer<>(props);
        }

        /**
         * Test synchronous producer performance
         */
        public void testSyncProducer(int messageCount, int messageSize) {
            logger.info("Testing synchronous producer: {} messages of {} bytes each", messageCount, messageSize);

            String message = generateMessage(messageSize);
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < messageCount; i++) {
                try {
                    ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "key-" + i, message);
                    Future<RecordMetadata> future = producer.send(record);
                    future.get(); // Wait for completion (synchronous)
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error sending message {}", i, e);
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            double throughputMsgsPerSec = (double) messageCount / (duration / 1000.0);
            double throughputMBPerSec = (throughputMsgsPerSec * messageSize) / (1024 * 1024);

            logger.info("Sync Producer Results:");
            logger.info("  Messages: {}", messageCount);
            logger.info("  Duration: {} ms", duration);
            logger.info("  Throughput: {:.2f} msgs/sec", throughputMsgsPerSec);
            logger.info("  Throughput: {:.2f} MB/sec", throughputMBPerSec);
            logger.info("  Avg Latency: {:.2f} ms/msg", (double) duration / messageCount);
        }

        /**
         * Test asynchronous producer performance
         */
        public void testAsyncProducer(int messageCount, int messageSize) {
            logger.info("Testing asynchronous producer: {} messages of {} bytes each", messageCount, messageSize);

            String message = generateMessage(messageSize);
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < messageCount; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "key-" + i, message);
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.error("Error sending message", exception);
                    }
                });
            }

            // Flush to ensure all messages are sent
            producer.flush();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            double throughputMsgsPerSec = (double) messageCount / (duration / 1000.0);
            double throughputMBPerSec = (throughputMsgsPerSec * messageSize) / (1024 * 1024);

            logger.info("Async Producer Results:");
            logger.info("  Messages: {}", messageCount);
            logger.info("  Duration: {} ms", duration);
            logger.info("  Throughput: {:.2f} msgs/sec", throughputMsgsPerSec);
            logger.info("  Throughput: {:.2f} MB/sec", throughputMBPerSec);
        }

        public void close() {
            producer.close();
        }
    }

    /**
     * Test consumer performance
     */
    public static class ConsumerPerformanceTest {
        private final KafkaConsumer<String, String> consumer;

        public ConsumerPerformanceTest(String bootstrapServers, String groupId, String topicName) {
            Properties props = KafkaConfig.createConsumerProperties(bootstrapServers, groupId);
            this.consumer = new KafkaConsumer<>(props);
            this.consumer.subscribe(Collections.singletonList(topicName));
        }

        /**
         * Test consumer performance
         */
        public void testConsumer(int expectedMessageCount, long timeoutMs) {
            logger.info("Testing consumer: expecting {} messages with {}ms timeout", expectedMessageCount, timeoutMs);

            long startTime = System.currentTimeMillis();
            int messageCount = 0;
            long totalBytes = 0;

            while (messageCount < expectedMessageCount) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) {
                    if (System.currentTimeMillis() - startTime > timeoutMs) {
                        logger.warn("Timeout reached, stopping consumer test");
                        break;
                    }
                    continue;
                }

                for (ConsumerRecord<String, String> record : records) {
                    messageCount++;
                    totalBytes += record.value().getBytes().length;
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (messageCount > 0) {
                double throughputMsgsPerSec = (double) messageCount / (duration / 1000.0);
                double throughputMBPerSec = (double) totalBytes / (1024 * 1024) / (duration / 1000.0);

                logger.info("Consumer Results:");
                logger.info("  Messages consumed: {}", messageCount);
                logger.info("  Duration: {} ms", duration);
                logger.info("  Throughput: {:.2f} msgs/sec", throughputMsgsPerSec);
                logger.info("  Throughput: {:.2f} MB/sec", throughputMBPerSec);
                logger.info("  Avg message size: {} bytes", totalBytes / messageCount);
            } else {
                logger.warn("No messages consumed during test");
            }
        }

        public void close() {
            consumer.close();
        }
    }

    /**
     * Generate a message of specified size
     */
    private static String generateMessage(int sizeBytes) {
        StringBuilder sb = new StringBuilder();
        String baseMessage = "This is a test message for performance testing. ";

        while (sb.length() < sizeBytes) {
            sb.append(baseMessage);
        }

        // Trim to exact size
        return sb.substring(0, Math.min(sizeBytes, sb.length()));
    }

    /**
     * Run a complete performance test
     */
    public static void runCompleteTest(String bootstrapServers, String topicName,
                                     int messageCount, int messageSize) {
        logger.info("=== Starting Complete Performance Test ===");
        logger.info("Topic: {}, Messages: {}, Size: {} bytes", topicName, messageCount, messageSize);

        // Test producer performance
        logger.info("\n--- Producer Performance Test ---");
        ProducerPerformanceTest producerTest = new ProducerPerformanceTest(bootstrapServers, topicName);

        try {
            producerTest.testAsyncProducer(messageCount, messageSize);
            Thread.sleep(2000); // Give time for messages to be written
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            producerTest.close();
        }

        // Test consumer performance
        logger.info("\n--- Consumer Performance Test ---");
        ConsumerPerformanceTest consumerTest = new ConsumerPerformanceTest(
            bootstrapServers, "perf-test-group", topicName);

        try {
            consumerTest.testConsumer(messageCount, 30000); // 30 second timeout
        } finally {
            consumerTest.close();
        }

        logger.info("\n=== Performance Test Complete ===");
    }

    /**
     * Main method for running performance tests
     */
    public static void main(String[] args) {
        String bootstrapServers = KafkaConfig.getBootstrapServers();
        String topicName = "performance-test-topic";

        // Parse command line arguments
        int messageCount = 1000;
        int messageSize = 1024; // 1KB

        if (args.length >= 1) {
            messageCount = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            messageSize = Integer.parseInt(args[1]);
        }

        runCompleteTest(bootstrapServers, topicName, messageCount, messageSize);
    }
}
