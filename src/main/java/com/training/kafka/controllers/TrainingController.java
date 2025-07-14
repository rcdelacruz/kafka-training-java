package com.training.kafka.controllers;

import com.training.kafka.services.Day01FoundationService;
import com.training.kafka.services.Day03ProducerService;
import com.training.kafka.services.Day04ConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for Kafka Training Course
 * 
 * Provides web endpoints to trigger training examples and demonstrations
 * for interactive learning through a web browser.
 * 
 * @author Kafka Training Course
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/training")
@CrossOrigin(origins = "*")
public class TrainingController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);
    
    private final Day01FoundationService day01Service;
    private final Day03ProducerService day03Service;
    private final Day04ConsumerService day04Service;
    
    public TrainingController(
            Day01FoundationService day01Service,
            Day03ProducerService day03Service,
            Day04ConsumerService day04Service) {
        this.day01Service = day01Service;
        this.day03Service = day03Service;
        this.day04Service = day04Service;
        logger.info("🌐 TrainingController initialized - Web API ready");
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Kafka Training Course");
        response.put("version", "1.0.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get available training modules
     */
    @GetMapping("/modules")
    public ResponseEntity<Map<String, Object>> getModules() {
        Map<String, Object> modules = new HashMap<>();
        
        modules.put("Day01Foundation", Map.of(
            "name", "Kafka Fundamentals",
            "description", "Basic topic operations and AdminClient usage",
            "endpoints", new String[]{"/day01/demo", "/day01/topics", "/day01/create-topic"}
        ));
        
        modules.put("Day03Producers", Map.of(
            "name", "Message Producers",
            "description", "Publishing messages to Kafka topics",
            "endpoints", new String[]{"/day03/demo", "/day03/send-message", "/day03/send-batch"}
        ));
        
        modules.put("Day04Consumers", Map.of(
            "name", "Message Consumers",
            "description", "Processing messages from Kafka topics",
            "endpoints", new String[]{"/day04/demo", "/day04/stats", "/day04/consume-raw"}
        ));
        
        return ResponseEntity.ok(modules);
    }
    
    // ===== Day 01 Foundation Endpoints =====
    
    @PostMapping("/day01/demo")
    public ResponseEntity<Map<String, String>> runDay01Demo() {
        logger.info("🎓 Running Day 1 Foundation Demo via Web API");
        
        try {
            day01Service.runDay01Demonstration();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 1 Foundation demonstration completed successfully");
            response.put("module", "Day01Foundation");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Day 1 demo failed: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 1 demonstration failed: " + e.getMessage());
            response.put("module", "Day01Foundation");
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/day01/topics")
    public ResponseEntity<Map<String, Object>> listTopics() {
        logger.info("📋 Listing topics via Web API");
        
        Set<String> topics = day01Service.listTopics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("topics", topics);
        response.put("count", topics.size());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/day01/create-topic")
    public ResponseEntity<Map<String, String>> createTopic(
            @RequestParam String name,
            @RequestParam(defaultValue = "3") int partitions,
            @RequestParam(defaultValue = "1") short replicationFactor) {
        
        logger.info("🔧 Creating topic '{}' via Web API", name);
        
        boolean success = day01Service.createTopic(name, partitions, replicationFactor);
        
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("status", "success");
            response.put("message", "Topic '" + name + "' created successfully");
        } else {
            response.put("status", "error");
            response.put("message", "Failed to create topic '" + name + "'");
        }
        response.put("topic", name);
        
        return ResponseEntity.ok(response);
    }
    
    // ===== Day 03 Producer Endpoints =====
    
    @PostMapping("/day03/demo")
    public ResponseEntity<Map<String, String>> runDay03Demo(
            @RequestParam(defaultValue = "user-events") String topic) {
        
        logger.info("🎓 Running Day 3 Producer Demo via Web API");
        
        try {
            day03Service.demonstrateProducerPatterns(topic);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 3 Producer demonstration completed successfully");
            response.put("module", "Day03Producers");
            response.put("topic", topic);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Day 3 demo failed: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 3 demonstration failed: " + e.getMessage());
            response.put("module", "Day03Producers");
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/day03/send-message")
    public ResponseEntity<Map<String, String>> sendMessage(
            @RequestParam(defaultValue = "user-events") String topic,
            @RequestParam String key,
            @RequestParam String message) {
        
        logger.info("📤 Sending message via Web API: topic={}, key={}", topic, key);
        
        boolean success = day03Service.sendMessageSyncSpring(topic, key, message);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "error");
        response.put("message", success ? "Message sent successfully" : "Failed to send message");
        response.put("topic", topic);
        response.put("key", key);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/day03/send-batch")
    public ResponseEntity<Map<String, String>> sendBatchMessages(
            @RequestParam(defaultValue = "user-events") String topic,
            @RequestParam(defaultValue = "10") int count) {
        
        logger.info("📤 Sending {} batch messages via Web API", count);
        
        try {
            day03Service.sendBatchMessagesSpring(topic, count);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", count + " messages sent successfully");
            response.put("topic", topic);
            response.put("count", String.valueOf(count));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Batch send failed: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to send batch messages: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // ===== Day 04 Consumer Endpoints =====
    
    @PostMapping("/day04/demo")
    public ResponseEntity<Map<String, String>> runDay04Demo() {
        logger.info("🎓 Running Day 4 Consumer Demo via Web API");
        
        try {
            day04Service.demonstrateConsumerPatterns();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 4 Consumer demonstration completed successfully");
            response.put("module", "Day04Consumers");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Day 4 demo failed: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 4 demonstration failed: " + e.getMessage());
            response.put("module", "Day04Consumers");
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/day04/stats")
    public ResponseEntity<Map<String, Object>> getConsumerStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("processedMessages", day04Service.getProcessedMessageCount());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/day04/consume-raw")
    public ResponseEntity<Map<String, String>> consumeRawMessages(
            @RequestParam(defaultValue = "user-events") String topic,
            @RequestParam(defaultValue = "raw-web-group") String groupId,
            @RequestParam(defaultValue = "5") int maxMessages) {
        
        logger.info("📥 Starting raw consumer via Web API");
        
        try {
            day04Service.consumeMessagesRaw(topic, groupId, maxMessages);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Raw consumer processed " + maxMessages + " messages");
            response.put("topic", topic);
            response.put("groupId", groupId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Raw consumer failed: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Raw consumer failed: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
