package com.training.kafka.examples;

import com.training.kafka.Day01Foundation.BasicTopicOperations;
import com.training.kafka.Day03Producers.SimpleProducer;
import com.training.kafka.Day04Consumers.SimpleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * E-commerce Order Processing System
 * 
 * A complete end-to-end example demonstrating Kafka concepts from Days 1-4:
 * 1. Topic creation and management (Day 1)
 * 2. Message partitioning and data flow (Day 2) 
 * 3. Reliable message production (Day 3)
 * 4. Scalable message consumption (Day 4)
 * 
 * This example simulates an e-commerce platform processing orders.
 */
public class EcommerceOrderSystem {
    private static final Logger logger = LoggerFactory.getLogger(EcommerceOrderSystem.class);
    
    // Configuration
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String ORDERS_TOPIC = "ecommerce-orders";
    private static final String NOTIFICATIONS_TOPIC = "order-notifications";
    private static final String ANALYTICS_TOPIC = "order-analytics";
    
    public static void main(String[] args) {
        logger.info("🛒 Starting E-commerce Order Processing System");
        
        try {
            // Step 1: Set up infrastructure (Day 1 - AdminClient)
            setupInfrastructure();
            
            // Step 2: Simulate order processing (Day 3 - Producers)
            processOrders();
            
            // Step 3: Handle notifications (Day 4 - Consumers)
            processNotifications();
            
            logger.info("✅ E-commerce system demonstration completed!");
            
        } catch (Exception e) {
            logger.error("❌ Error in e-commerce system", e);
        }
    }
    
    /**
     * Day 1: Create topics and set up infrastructure
     */
    private static void setupInfrastructure() {
        logger.info("\n=== Step 1: Infrastructure Setup (Day 1) ===");
        logger.info("Setting up Kafka topics for e-commerce platform...");
        
        BasicTopicOperations admin = new BasicTopicOperations(BOOTSTRAP_SERVERS);
        
        try {
            // Orders topic - partitioned by customer for ordering guarantees
            admin.createTopic(ORDERS_TOPIC, 6, (short) 1);
            logger.info("✅ Created orders topic with 6 partitions for customer-based partitioning");
            
            // Notifications topic - high throughput for real-time alerts
            admin.createTopic(NOTIFICATIONS_TOPIC, 3, (short) 1);
            logger.info("✅ Created notifications topic for real-time customer alerts");
            
            // Analytics topic - for business intelligence
            admin.createTopic(ANALYTICS_TOPIC, 3, (short) 1);
            logger.info("✅ Created analytics topic for business intelligence");
            
            // Display topic information
            admin.listTopics();
            
        } finally {
            admin.close();
        }
    }
    
    /**
     * Day 3: Process orders using reliable producers
     */
    private static void processOrders() {
        logger.info("\n=== Step 2: Order Processing (Day 3) ===");
        logger.info("Processing customer orders with reliable producers...");
        
        SimpleProducer orderProducer = new SimpleProducer(BOOTSTRAP_SERVERS, ORDERS_TOPIC);
        SimpleProducer notificationProducer = new SimpleProducer(BOOTSTRAP_SERVERS, NOTIFICATIONS_TOPIC);
        SimpleProducer analyticsProducer = new SimpleProducer(BOOTSTRAP_SERVERS, ANALYTICS_TOPIC);
        
        try {
            // Simulate different customer orders
            String[] customers = {"customer-alice", "customer-bob", "customer-charlie"};
            String[] products = {"laptop", "smartphone", "headphones", "tablet", "smartwatch"};
            
            for (int i = 1; i <= 10; i++) {
                String customerId = customers[i % customers.length];
                String product = products[i % products.length];
                double amount = 99.99 + (i * 10);
                
                // Create order message (JSON format)
                String orderMessage = String.format(
                    "{\"orderId\":\"%d\",\"customerId\":\"%s\",\"product\":\"%s\",\"amount\":%.2f,\"timestamp\":\"%s\",\"status\":\"pending\"}",
                    i, customerId, product, amount, java.time.Instant.now()
                );
                
                // Send to orders topic with customer ID as key (ensures ordering per customer)
                orderProducer.sendMessageAsync(customerId, orderMessage);
                logger.info("📦 Order {} processed for {}: {} (${:.2f})", i, customerId, product, amount);
                
                // Send notification
                String notificationMessage = String.format(
                    "{\"customerId\":\"%s\",\"type\":\"order_confirmation\",\"orderId\":\"%d\",\"message\":\"Your order for %s has been confirmed\"}",
                    customerId, i, product
                );
                notificationProducer.sendMessageAsync(customerId, notificationMessage);
                
                // Send analytics event
                String analyticsMessage = String.format(
                    "{\"event\":\"order_placed\",\"customerId\":\"%s\",\"product\":\"%s\",\"amount\":%.2f,\"category\":\"%s\"}",
                    customerId, product, amount, getProductCategory(product)
                );
                analyticsProducer.sendMessageAsync("analytics", analyticsMessage);
                
                // Simulate processing time
                Thread.sleep(500);
            }
            
            logger.info("✅ All orders processed successfully with reliable delivery!");
            
        } catch (Exception e) {
            logger.error("Error processing orders", e);
        } finally {
            orderProducer.close();
            notificationProducer.close();
            analyticsProducer.close();
        }
    }
    
    /**
     * Day 4: Process notifications using consumer groups
     */
    private static void processNotifications() {
        logger.info("\n=== Step 3: Notification Processing (Day 4) ===");
        logger.info("Processing notifications with scalable consumer groups...");
        
        // Simulate multiple notification processors (email, SMS, push)
        Thread emailProcessor = new Thread(() -> {
            SimpleConsumer consumer = new SimpleConsumer(BOOTSTRAP_SERVERS, "email-notification-group");
            try {
                logger.info("📧 Email processor started");
                consumer.consumeWithManualOffsetManagement(NOTIFICATIONS_TOPIC, 5);
            } finally {
                logger.info("📧 Email processor stopped");
            }
        }, "EmailProcessor");
        
        Thread smsProcessor = new Thread(() -> {
            SimpleConsumer consumer = new SimpleConsumer(BOOTSTRAP_SERVERS, "sms-notification-group");
            try {
                logger.info("📱 SMS processor started");
                consumer.consumeWithManualOffsetManagement(NOTIFICATIONS_TOPIC, 5);
            } finally {
                logger.info("📱 SMS processor stopped");
            }
        }, "SMSProcessor");
        
        Thread analyticsProcessor = new Thread(() -> {
            SimpleConsumer consumer = new SimpleConsumer(BOOTSTRAP_SERVERS, "analytics-group");
            try {
                logger.info("📊 Analytics processor started");
                consumer.consumeWithManualOffsetManagement(ANALYTICS_TOPIC, 8);
            } finally {
                logger.info("📊 Analytics processor stopped");
            }
        }, "AnalyticsProcessor");
        
        // Start all processors
        emailProcessor.start();
        smsProcessor.start();
        analyticsProcessor.start();
        
        try {
            // Let them run for a while
            emailProcessor.join(15000);
            smsProcessor.join(15000);
            analyticsProcessor.join(15000);
            
            logger.info("✅ All notification processors completed!");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Notification processing interrupted", e);
        }
    }
    
    /**
     * Helper method to categorize products
     */
    private static String getProductCategory(String product) {
        switch (product.toLowerCase()) {
            case "laptop":
            case "tablet":
                return "computers";
            case "smartphone":
                return "mobile";
            case "headphones":
            case "smartwatch":
                return "accessories";
            default:
                return "other";
        }
    }
    
    /**
     * Display the key concepts demonstrated
     */
    public static void printSystemArchitecture() {
        logger.info("\n🏗️ E-COMMERCE SYSTEM ARCHITECTURE:");
        logger.info("");
        logger.info("Day 1 - Infrastructure (AdminClient):");
        logger.info("  ✓ Created 3 topics with appropriate partitioning");
        logger.info("  ✓ Orders topic: 6 partitions (customer-based routing)");
        logger.info("  ✓ Notifications: 3 partitions (high throughput)");
        logger.info("  ✓ Analytics: 3 partitions (business intelligence)");
        logger.info("");
        logger.info("Day 2 - Data Flow Concepts:");
        logger.info("  ✓ Customer ID as message key ensures ordering per customer");
        logger.info("  ✓ Different topics for different business functions");
        logger.info("  ✓ Partitioning strategy based on business requirements");
        logger.info("");
        logger.info("Day 3 - Reliable Producers:");
        logger.info("  ✓ Asynchronous message sending for performance");
        logger.info("  ✓ Key-based partitioning for customer order sequences");
        logger.info("  ✓ JSON message format for cross-team compatibility");
        logger.info("  ✓ Error handling and retry mechanisms");
        logger.info("");
        logger.info("Day 4 - Scalable Consumers:");
        logger.info("  ✓ Multiple consumer groups for different services");
        logger.info("  ✓ Email notification service (independent scaling)");
        logger.info("  ✓ SMS notification service (independent scaling)");
        logger.info("  ✓ Analytics service (business intelligence)");
        logger.info("  ✓ Manual offset management for reliability");
        logger.info("");
        logger.info("🎯 This demonstrates a production-ready e-commerce order system!");
    }
}
