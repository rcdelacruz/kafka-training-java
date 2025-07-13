package com.training.kafka.Day08Advanced;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Day 8: Monitoring and Metrics
 * 
 * Demonstrates comprehensive monitoring of Kafka clients and cluster:
 * - Producer metrics collection
 * - Consumer metrics monitoring
 * - Cluster health monitoring
 * - Performance metrics analysis
 * - Alerting based on thresholds
 */
public class MonitoringExample {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringExample.class);
    
    private final AdminClient adminClient;
    private final ScheduledExecutorService scheduler;
    
    // Alerting thresholds
    private static final double MAX_REQUEST_LATENCY_MS = 100.0;
    private static final double MIN_RECORD_SEND_RATE = 10.0;
    private static final double MAX_CONSUMER_LAG = 1000.0;
    
    public MonitoringExample(String bootstrapServers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "monitoring-admin");
        
        this.adminClient = AdminClient.create(props);
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        logger.info("Monitoring system initialized");
    }
    
    /**
     * Monitor producer metrics
     */
    public void monitorProducerMetrics(KafkaProducer<String, String> producer) {
        logger.info("Starting producer metrics monitoring...");
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<MetricName, ? extends Metric> metrics = producer.metrics();
                
                // Key producer metrics to monitor
                double recordSendRate = getMetricValue(metrics, "record-send-rate");
                double batchSizeAvg = getMetricValue(metrics, "batch-size-avg");
                double requestLatencyAvg = getMetricValue(metrics, "request-latency-avg");
                double recordErrorRate = getMetricValue(metrics, "record-error-rate");
                double recordRetryRate = getMetricValue(metrics, "record-retry-rate");
                double bufferAvailableBytes = getMetricValue(metrics, "buffer-available-bytes");
                double compressionRateAvg = getMetricValue(metrics, "compression-rate-avg");
                
                // Log metrics
                logger.info("Producer Metrics:");
                logger.info("  Record Send Rate: {:.2f} records/sec", recordSendRate);
                logger.info("  Batch Size Avg: {:.2f} bytes", batchSizeAvg);
                logger.info("  Request Latency Avg: {:.2f} ms", requestLatencyAvg);
                logger.info("  Record Error Rate: {:.2f} errors/sec", recordErrorRate);
                logger.info("  Record Retry Rate: {:.2f} retries/sec", recordRetryRate);
                logger.info("  Buffer Available: {:.2f} bytes", bufferAvailableBytes);
                logger.info("  Compression Rate: {:.2f}%", compressionRateAvg * 100);
                
                // Check thresholds and alert
                checkProducerAlerts(recordSendRate, requestLatencyAvg, recordErrorRate);
                
            } catch (Exception e) {
                logger.error("Error collecting producer metrics", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Monitor consumer metrics
     */
    public void monitorConsumerMetrics(KafkaConsumer<String, String> consumer) {
        logger.info("Starting consumer metrics monitoring...");
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<MetricName, ? extends Metric> metrics = consumer.metrics();
                
                // Key consumer metrics to monitor
                double recordsConsumedRate = getMetricValue(metrics, "records-consumed-rate");
                double bytesConsumedRate = getMetricValue(metrics, "bytes-consumed-rate");
                double fetchLatencyAvg = getMetricValue(metrics, "fetch-latency-avg");
                double recordsLagMax = getMetricValue(metrics, "records-lag-max");
                double recordsLagAvg = getMetricValue(metrics, "records-lag-avg");
                double fetchRate = getMetricValue(metrics, "fetch-rate");
                double commitRate = getMetricValue(metrics, "commit-rate");
                
                // Log metrics
                logger.info("Consumer Metrics:");
                logger.info("  Records Consumed Rate: {:.2f} records/sec", recordsConsumedRate);
                logger.info("  Bytes Consumed Rate: {:.2f} bytes/sec", bytesConsumedRate);
                logger.info("  Fetch Latency Avg: {:.2f} ms", fetchLatencyAvg);
                logger.info("  Records Lag Max: {:.2f}", recordsLagMax);
                logger.info("  Records Lag Avg: {:.2f}", recordsLagAvg);
                logger.info("  Fetch Rate: {:.2f} fetches/sec", fetchRate);
                logger.info("  Commit Rate: {:.2f} commits/sec", commitRate);
                
                // Check thresholds and alert
                checkConsumerAlerts(recordsLagMax, fetchLatencyAvg, recordsConsumedRate);
                
            } catch (Exception e) {
                logger.error("Error collecting consumer metrics", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Monitor cluster health
     */
    public void monitorClusterHealth() {
        logger.info("Starting cluster health monitoring...");
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                DescribeClusterResult clusterResult = adminClient.describeCluster();
                
                String clusterId = clusterResult.clusterId().get();
                int nodeCount = clusterResult.nodes().get().size();
                String controllerId = clusterResult.controller().get().idString();
                
                logger.info("Cluster Health:");
                logger.info("  Cluster ID: {}", clusterId);
                logger.info("  Node Count: {}", nodeCount);
                logger.info("  Controller: {}", controllerId);
                
                // Additional health checks would go here:
                // - Topic replication status
                // - Partition leader distribution
                // - Broker disk usage
                // - Network connectivity
                
                if (nodeCount < 3) {
                    logger.warn("ALERT: Cluster has fewer than 3 nodes ({}). Consider scaling up.", nodeCount);
                }
                
            } catch (Exception e) {
                logger.error("Error monitoring cluster health", e);
            }
        }, 0, 60, TimeUnit.SECONDS);
    }
    
    /**
     * Create a sample producer for monitoring
     */
    public KafkaProducer<String, String> createMonitoredProducer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "monitored-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Enable metrics collection
        props.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, "10000");
        props.put(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, "6");
        
        return new KafkaProducer<>(props);
    }
    
    /**
     * Create a sample consumer for monitoring
     */
    public KafkaConsumer<String, String> createMonitoredConsumer(String bootstrapServers, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "monitored-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Enable metrics collection
        props.put(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, "10000");
        props.put(ConsumerConfig.METRICS_NUM_SAMPLES_CONFIG, "6");
        
        return new KafkaConsumer<>(props);
    }
    
    /**
     * Extract metric value by name
     */
    private double getMetricValue(Map<MetricName, ? extends Metric> metrics, String metricName) {
        return metrics.entrySet().stream()
            .filter(entry -> entry.getKey().name().equals(metricName))
            .mapToDouble(entry -> (Double) entry.getValue().metricValue())
            .findFirst()
            .orElse(0.0);
    }
    
    /**
     * Check producer alerts based on thresholds
     */
    private void checkProducerAlerts(double recordSendRate, double requestLatencyAvg, double recordErrorRate) {
        if (requestLatencyAvg > MAX_REQUEST_LATENCY_MS) {
            logger.warn("ALERT: High producer request latency: {:.2f} ms (threshold: {:.2f} ms)", 
                requestLatencyAvg, MAX_REQUEST_LATENCY_MS);
        }
        
        if (recordSendRate < MIN_RECORD_SEND_RATE) {
            logger.warn("ALERT: Low producer send rate: {:.2f} records/sec (threshold: {:.2f} records/sec)", 
                recordSendRate, MIN_RECORD_SEND_RATE);
        }
        
        if (recordErrorRate > 0) {
            logger.error("ALERT: Producer errors detected: {:.2f} errors/sec", recordErrorRate);
        }
    }
    
    /**
     * Check consumer alerts based on thresholds
     */
    private void checkConsumerAlerts(double recordsLagMax, double fetchLatencyAvg, double recordsConsumedRate) {
        if (recordsLagMax > MAX_CONSUMER_LAG) {
            logger.warn("ALERT: High consumer lag: {:.2f} records (threshold: {:.2f} records)", 
                recordsLagMax, MAX_CONSUMER_LAG);
        }
        
        if (fetchLatencyAvg > MAX_REQUEST_LATENCY_MS) {
            logger.warn("ALERT: High consumer fetch latency: {:.2f} ms (threshold: {:.2f} ms)", 
                fetchLatencyAvg, MAX_REQUEST_LATENCY_MS);
        }
        
        if (recordsConsumedRate == 0) {
            logger.warn("ALERT: Consumer not consuming any records");
        }
    }
    
    /**
     * Print monitoring dashboard
     */
    public void printMonitoringDashboard(KafkaProducer<String, String> producer, 
                                        KafkaConsumer<String, String> consumer) {
        logger.info("=== Kafka Monitoring Dashboard ===");
        
        try {
            // Producer metrics
            Map<MetricName, ? extends Metric> producerMetrics = producer.metrics();
            logger.info("Producer Status:");
            logger.info("  Send Rate: {:.2f} records/sec", 
                getMetricValue(producerMetrics, "record-send-rate"));
            logger.info("  Error Rate: {:.2f} errors/sec", 
                getMetricValue(producerMetrics, "record-error-rate"));
            logger.info("  Latency: {:.2f} ms", 
                getMetricValue(producerMetrics, "request-latency-avg"));
            
            // Consumer metrics
            Map<MetricName, ? extends Metric> consumerMetrics = consumer.metrics();
            logger.info("Consumer Status:");
            logger.info("  Consume Rate: {:.2f} records/sec", 
                getMetricValue(consumerMetrics, "records-consumed-rate"));
            logger.info("  Lag: {:.2f} records", 
                getMetricValue(consumerMetrics, "records-lag-max"));
            logger.info("  Fetch Latency: {:.2f} ms", 
                getMetricValue(consumerMetrics, "fetch-latency-avg"));
            
            // Cluster info
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            logger.info("Cluster Status:");
            logger.info("  Nodes: {}", clusterResult.nodes().get().size());
            logger.info("  Controller: {}", clusterResult.controller().get().idString());
            
        } catch (Exception e) {
            logger.error("Error generating monitoring dashboard", e);
        }
        
        logger.info("=================================");
    }
    
    /**
     * Print monitoring best practices
     */
    public static void printMonitoringBestPractices() {
        logger.info("Kafka Monitoring Best Practices:");
        logger.info("");
        
        logger.info("1. Key Metrics to Monitor:");
        logger.info("   Producer:");
        logger.info("   - record-send-rate (throughput)");
        logger.info("   - record-error-rate (errors)");
        logger.info("   - request-latency-avg (latency)");
        logger.info("   - batch-size-avg (efficiency)");
        logger.info("   Consumer:");
        logger.info("   - records-consumed-rate (throughput)");
        logger.info("   - records-lag-max (lag)");
        logger.info("   - fetch-latency-avg (latency)");
        logger.info("");
        
        logger.info("2. Monitoring Tools:");
        logger.info("   - JMX metrics collection");
        logger.info("   - Prometheus + Grafana");
        logger.info("   - Confluent Control Center");
        logger.info("   - Kafka Manager/AKHQ");
        logger.info("   - Custom monitoring applications");
        logger.info("");
        
        logger.info("3. Alerting Strategies:");
        logger.info("   - Set appropriate thresholds");
        logger.info("   - Implement escalation policies");
        logger.info("   - Monitor trends, not just snapshots");
        logger.info("   - Include business context in alerts");
        logger.info("");
        
        logger.info("4. Performance Optimization:");
        logger.info("   - Monitor batch sizes and compression");
        logger.info("   - Track memory and CPU usage");
        logger.info("   - Monitor network utilization");
        logger.info("   - Analyze partition distribution");
    }
    
    /**
     * Shutdown the monitoring system
     */
    public void shutdown() {
        logger.info("Shutting down monitoring system...");
        scheduler.shutdown();
        adminClient.close();
        
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Monitoring system shut down");
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String groupId = "monitoring-demo-group";
        
        MonitoringExample monitor = new MonitoringExample(bootstrapServers);
        
        // Print best practices
        printMonitoringBestPractices();
        
        try {
            // Create monitored clients
            KafkaProducer<String, String> producer = monitor.createMonitoredProducer(bootstrapServers);
            KafkaConsumer<String, String> consumer = monitor.createMonitoredConsumer(bootstrapServers, groupId);
            
            // Start monitoring
            monitor.monitorClusterHealth();
            monitor.monitorProducerMetrics(producer);
            monitor.monitorConsumerMetrics(consumer);
            
            // Generate some sample data for metrics
            logger.info("Generating sample data for monitoring...");
            
            // Send some messages
            for (int i = 0; i < 10; i++) {
                producer.send(new org.apache.kafka.clients.producer.ProducerRecord<>(
                    "user-events", "key" + i, "Sample message " + i));
            }
            producer.flush();
            
            // Print dashboard periodically
            scheduler.scheduleAtFixedRate(() -> 
                monitor.printMonitoringDashboard(producer, consumer),
                60, 60, TimeUnit.SECONDS);
            
            // Run for 5 minutes then shutdown
            Thread.sleep(300000);
            
            producer.close();
            consumer.close();
            
        } catch (Exception e) {
            logger.error("Error in monitoring demo", e);
        } finally {
            monitor.shutdown();
        }
    }
}