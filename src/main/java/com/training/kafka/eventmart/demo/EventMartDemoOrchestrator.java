package com.training.kafka.eventmart.demo;

import com.training.kafka.eventmart.events.EventMartEvents.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * EventMart Demo Orchestrator
 *
 * Final Demo Showcase: Orchestrates a complete EventMart demonstration
 * showing all Kafka concepts learned throughout the 8-day training.
 *
 * This class demonstrates:
 * - Complete e-commerce user journey
 * - Real-time event generation and processing
 * - Integration of all EventMart services
 * - Live metrics and monitoring
 * - Production-ready event streaming
 */
public class EventMartDemoOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(EventMartDemoOrchestrator.class);

    private final KafkaProducer<String, String> producer;
    private final ScheduledExecutorService scheduler;
    private final Random random = new Random();

    // Demo configuration
    private static final int DEMO_DURATION_MINUTES = 10;
    private static final int USERS_TO_SIMULATE = 50;
    private static final int PRODUCTS_IN_CATALOG = 20;
    private static final double ORDER_PROBABILITY = 0.3; // 30% chance user places order

    // Demo state tracking
    private final List<String> activeUsers = new ArrayList<>();
    private final List<String> productCatalog = new ArrayList<>();
    private final Map<String, Integer> productInventory = new HashMap<>();
    private final DemoMetrics metrics = new DemoMetrics();

    public EventMartDemoOrchestrator(KafkaProducer<String, String> producer) {
        this.producer = producer;
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    /**
     * Start the complete EventMart demonstration
     */
    public void startDemo() {
        logger.info("🚀 Starting EventMart Final Demo Showcase");
        logger.info("Duration: {} minutes", DEMO_DURATION_MINUTES);
        logger.info("Simulating {} users with {} products", USERS_TO_SIMULATE, PRODUCTS_IN_CATALOG);

        // Phase 1: Initialize the platform
        initializePlatform();

        // Phase 2: Simulate user activity
        simulateUserActivity();

        // Phase 3: Generate business events
        generateBusinessEvents();

        // Phase 4: Show real-time metrics
        showRealTimeMetrics();

        logger.info("✅ EventMart Demo is now running!");
        logger.info("Watch the real-time event stream and metrics...");
    }

    /**
     * Phase 1: Initialize the EventMart platform
     */
    private void initializePlatform() {
        logger.info("📋 Phase 1: Initializing EventMart Platform");

        // Create product catalog
        createProductCatalog();

        // Register initial users
        registerInitialUsers();

        logger.info("✅ Platform initialized with {} products and {} users",
                   productCatalog.size(), activeUsers.size());
    }

    /**
     * Create a realistic product catalog
     */
    private void createProductCatalog() {
        String[] categories = {"Electronics", "Books", "Clothing", "Home", "Sports"};
        String[] productNames = {
            "Laptop Pro 15", "Smartphone X", "Wireless Headphones", "4K Monitor",
            "Programming Guide", "Design Patterns", "Clean Code", "System Design",
            "Cotton T-Shirt", "Jeans Classic", "Running Shoes", "Winter Jacket",
            "Coffee Maker", "Desk Lamp", "Office Chair", "Storage Box",
            "Yoga Mat", "Dumbbells", "Tennis Racket", "Fitness Tracker"
        };

        for (int i = 0; i < PRODUCTS_IN_CATALOG; i++) {
            String productId = "product-" + (i + 1);
            String name = productNames[i % productNames.length];
            String category = categories[i % categories.length];
            double price = 19.99 + (random.nextDouble() * 500);
            int inventory = 50 + random.nextInt(200);

            ProductCreated event = new ProductCreated(productId, name, category, price, inventory);
            sendEvent("eventmart-products", productId, event.toJson());

            productCatalog.add(productId);
            productInventory.put(productId, inventory);
            metrics.productsCreated++;
        }
    }

    /**
     * Register initial users
     */
    private void registerInitialUsers() {
        String[] firstNames = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        String[] sources = {"web", "mobile", "social", "referral"};

        for (int i = 0; i < USERS_TO_SIMULATE; i++) {
            String userId = "user-" + (i + 1);
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com";
            String source = sources[random.nextInt(sources.length)];

            UserRegistered event = new UserRegistered(userId, email, firstName, lastName, source);
            sendEvent("eventmart-users", userId, event.toJson());

            activeUsers.add(userId);
            metrics.usersRegistered++;
        }
    }

    /**
     * Phase 2: Simulate realistic user activity
     */
    private void simulateUserActivity() {
        logger.info("👥 Phase 2: Simulating User Activity");

        // Simulate user sessions every 5-15 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                simulateUserSession();
            } catch (Exception e) {
                logger.error("Error in user session simulation", e);
            }
        }, 2, 5 + random.nextInt(10), TimeUnit.SECONDS);
    }

    /**
     * Simulate a single user session
     */
    private void simulateUserSession() {
        if (activeUsers.isEmpty() || productCatalog.isEmpty()) return;

        String userId = activeUsers.get(random.nextInt(activeUsers.size()));

        // User browses products (always happens)
        simulateProductBrowsing(userId);

        // User might place an order (30% chance)
        if (random.nextDouble() < ORDER_PROBABILITY) {
            simulateOrderPlacement(userId);
        }
    }

    /**
     * Simulate product browsing activity
     */
    private void simulateProductBrowsing(String userId) {
        // Simulate page views, searches, etc.
        // This would generate analytics events
        metrics.pageViews++;
    }

    /**
     * Simulate order placement and fulfillment
     */
    private void simulateOrderPlacement(String userId) {
        String orderId = "order-" + System.currentTimeMillis() + "-" + random.nextInt(1000);

        // Select 1-3 random products
        int itemCount = 1 + random.nextInt(3);
        List<OrderItem> items = new ArrayList<>();
        double totalAmount = 0;

        for (int i = 0; i < itemCount; i++) {
            String productId = productCatalog.get(random.nextInt(productCatalog.size()));
            int quantity = 1 + random.nextInt(3);
            double unitPrice = 19.99 + (random.nextDouble() * 500);

            items.add(new OrderItem(productId, quantity, unitPrice));
            totalAmount += quantity * unitPrice;

            // Update inventory
            int currentInventory = productInventory.getOrDefault(productId, 0);
            if (currentInventory >= quantity) {
                productInventory.put(productId, currentInventory - quantity);

                // Generate inventory change event
                InventoryChanged invEvent = new InventoryChanged(
                    productId, currentInventory, currentInventory - quantity, "order-fulfillment");
                sendEvent("eventmart-products", productId, invEvent.toJson());
            }
        }

        // Create order
        Address shippingAddress = new Address(
            "123 Main St", "Anytown", "CA", "12345", "USA");

        OrderPlaced orderEvent = new OrderPlaced(
            orderId, userId, items.toArray(new OrderItem[0]), totalAmount, shippingAddress);
        sendEvent("eventmart-orders", orderId, orderEvent.toJson());

        // Schedule order confirmation (2-5 seconds later)
        scheduler.schedule(() -> confirmOrder(orderId), 2 + random.nextInt(3), TimeUnit.SECONDS);

        // Process payment
        processPayment(orderId, totalAmount);

        metrics.ordersPlaced++;
        metrics.totalRevenue += totalAmount;
    }

    /**
     * Confirm an order
     */
    private void confirmOrder(String orderId) {
        String confirmationNumber = "CONF-" + System.currentTimeMillis();
        Instant estimatedDelivery = Instant.now().plus(3 + random.nextInt(7), ChronoUnit.DAYS);

        OrderConfirmed event = new OrderConfirmed(orderId, confirmationNumber, estimatedDelivery);
        sendEvent("eventmart-orders", orderId, event.toJson());

        metrics.ordersConfirmed++;
    }

    /**
     * Process payment for an order
     */
    private void processPayment(String orderId, double amount) {
        String paymentId = "pay-" + System.currentTimeMillis();
        String[] methods = {"credit_card", "debit_card", "paypal", "apple_pay"};
        String method = methods[random.nextInt(methods.length)];

        // Initiate payment
        PaymentInitiated initEvent = new PaymentInitiated(paymentId, orderId, amount, method);
        sendEvent("eventmart-payments", paymentId, initEvent.toJson());

        // Complete payment (90% success rate)
        scheduler.schedule(() -> {
            if (random.nextDouble() < 0.9) {
                String transactionId = "txn-" + System.currentTimeMillis();
                PaymentCompleted completeEvent = new PaymentCompleted(
                    paymentId, orderId, transactionId, amount);
                sendEvent("eventmart-payments", paymentId, completeEvent.toJson());

                metrics.paymentsCompleted++;

                // Send notification
                sendOrderNotification(orderId, "Order confirmed and payment processed");
            } else {
                // Payment failed - would generate PaymentFailed event
                logger.warn("Payment failed for order: {}", orderId);
            }
        }, 1 + random.nextInt(2), TimeUnit.SECONDS);
    }

    /**
     * Send order notification
     */
    private void sendOrderNotification(String orderId, String message) {
        String notificationId = "notif-" + System.currentTimeMillis();
        String userId = "user-" + random.nextInt(USERS_TO_SIMULATE); // Simplified

        NotificationSent event = new NotificationSent(
            notificationId, userId, "email", "Order Update", message);
        sendEvent("eventmart-notifications", notificationId, event.toJson());

        metrics.notificationsSent++;
    }

    /**
     * Phase 3: Generate additional business events
     */
    private void generateBusinessEvents() {
        logger.info("📊 Phase 3: Generating Business Events");

        // Periodic inventory updates
        scheduler.scheduleAtFixedRate(() -> {
            generateInventoryUpdates();
        }, 30, 45, TimeUnit.SECONDS);

        // User profile updates
        scheduler.scheduleAtFixedRate(() -> {
            generateUserUpdates();
        }, 20, 30, TimeUnit.SECONDS);
    }

    /**
     * Generate inventory updates
     */
    private void generateInventoryUpdates() {
        if (productCatalog.isEmpty()) return;

        String productId = productCatalog.get(random.nextInt(productCatalog.size()));
        int currentInventory = productInventory.getOrDefault(productId, 0);
        int newInventory = currentInventory + 10 + random.nextInt(50); // Restock

        productInventory.put(productId, newInventory);

        InventoryChanged event = new InventoryChanged(
            productId, currentInventory, newInventory, "restock");
        sendEvent("eventmart-products", productId, event.toJson());
    }

    /**
     * Generate user profile updates
     */
    private void generateUserUpdates() {
        if (activeUsers.isEmpty()) return;

        String userId = activeUsers.get(random.nextInt(activeUsers.size()));
        Map<String, Object> updates = new HashMap<>();
        updates.put("last_login", Instant.now().toString());
        updates.put("preferences_updated", true);

        UserUpdated event = new UserUpdated(userId, updates);
        sendEvent("eventmart-users", userId, event.toJson());
    }

    /**
     * Phase 4: Show real-time metrics
     */
    private void showRealTimeMetrics() {
        logger.info("📈 Phase 4: Real-time Metrics Dashboard");

        // Print metrics every 30 seconds
        scheduler.scheduleAtFixedRate(() -> {
            printMetricsDashboard();
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Print live metrics dashboard
     */
    private void printMetricsDashboard() {
        logger.info("=== EventMart Live Metrics Dashboard ===");
        logger.info("Users Registered: {}", metrics.usersRegistered);
        logger.info("Products Created: {}", metrics.productsCreated);
        logger.info("Orders Placed: {}", metrics.ordersPlaced);
        logger.info("Orders Confirmed: {}", metrics.ordersConfirmed);
        logger.info("Payments Completed: {}", metrics.paymentsCompleted);
        logger.info("Notifications Sent: {}", metrics.notificationsSent);
        logger.info("Page Views: {}", metrics.pageViews);
        logger.info("Total Revenue: ${:.2f}", metrics.totalRevenue);
        logger.info("Average Order Value: ${:.2f}",
                   metrics.ordersPlaced > 0 ? metrics.totalRevenue / metrics.ordersPlaced : 0);
        logger.info("==========================================");
    }

    /**
     * Send event to Kafka topic
     */
    private void sendEvent(String topic, String key, String value) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    logger.error("Error sending event to {}: {}", topic, exception.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error creating producer record", e);
        }
    }

    /**
     * Stop the demo
     */
    public void stopDemo() {
        logger.info("🛑 Stopping EventMart Demo");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Final metrics
        printMetricsDashboard();
        logger.info("✅ EventMart Demo completed successfully!");
    }

    /**
     * Demo metrics tracking
     */
    private static class DemoMetrics {
        int usersRegistered = 0;
        int productsCreated = 0;
        int ordersPlaced = 0;
        int ordersConfirmed = 0;
        int paymentsCompleted = 0;
        int notificationsSent = 0;
        int pageViews = 0;
        double totalRevenue = 0.0;
    }

    /**
     * Main method to run the EventMart Final Demo
     */
    public static void main(String[] args) {
        logger.info("🎭 EventMart Final Demo Showcase");
        logger.info("This demo showcases all Kafka concepts learned in the 8-day training");

        // Create producer
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("enable.idempotence", true);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            EventMartDemoOrchestrator demo = new EventMartDemoOrchestrator(producer);

            // Start the demo
            demo.startDemo();

            // Run for the specified duration
            Thread.sleep(DEMO_DURATION_MINUTES * 60 * 1000);

            // Stop the demo
            demo.stopDemo();

        } catch (Exception e) {
            logger.error("Error running EventMart demo", e);
        }
    }
}
