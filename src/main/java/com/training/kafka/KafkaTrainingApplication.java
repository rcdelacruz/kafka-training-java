package com.training.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main Spring Boot Application for Kafka Training Course
 * 
 * This application serves as the entry point for the comprehensive Kafka training course.
 * It provides both web-based interfaces and command-line execution capabilities for
 * all training modules and the EventMart progressive project.
 * 
 * Features:
 * - Web interface for interactive training modules
 * - REST endpoints for triggering examples and demos
 * - Command-line execution support for traditional examples
 * - Integration with all day-by-day training components
 * - EventMart progressive project coordination
 * 
 * Usage:
 * - Web Interface: Start the application and visit http://localhost:8080
 * - Command Line: Use --spring.main.web-application-type=none for CLI mode
 * - Specific Examples: Use --training.module=Day01Foundation --training.example=BasicTopicOperations
 * 
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
public class KafkaTrainingApplication {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTrainingApplication.class);

    public static void main(String[] args) {
        logger.info("🚀 Starting Kafka Training Application with Spring Boot");
        logger.info("📚 Comprehensive Apache Kafka Training Course - From Beginner to Advanced");
        logger.info("🎭 Includes EventMart Progressive Project for hands-on learning");
        
        SpringApplication app = new SpringApplication(KafkaTrainingApplication.class);
        
        // Check if running in CLI mode
        boolean isCliMode = isCommandLineMode(args);
        if (isCliMode) {
            app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
            logger.info("🖥️  Running in Command Line mode");
        } else {
            logger.info("🌐 Starting Web Application mode");
            logger.info("📱 Web interface will be available at: http://localhost:8080");
        }
        
        app.run(args);
    }

    /**
     * Command line runner for executing specific training modules
     */
    @Bean
    public CommandLineRunner trainingRunner() {
        return args -> {
            if (isCommandLineMode(args)) {
                logger.info("🎯 Command Line Training Mode Activated");
                executeCommandLineTraining(args);
            } else {
                logger.info("🎉 Kafka Training Application Started Successfully!");
                logger.info("📖 Visit http://localhost:8080 for the interactive training interface");
                logger.info("🎭 Access EventMart Progressive Project at http://localhost:8080/eventmart");
                logger.info("📊 View training progress and examples at http://localhost:8080/training");
            }
        };
    }

    /**
     * Check if the application should run in command line mode
     */
    private static boolean isCommandLineMode(String[] args) {
        for (String arg : args) {
            if (arg.contains("web-application-type=none") || 
                arg.contains("training.module") || 
                arg.contains("exec.mainClass")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Execute specific training modules from command line
     */
    private void executeCommandLineTraining(String[] args) {
        logger.info("🔧 Parsing command line arguments for training execution");
        
        String module = extractArgument(args, "training.module");
        String example = extractArgument(args, "training.example");
        String mainClass = extractArgument(args, "exec.mainClass");
        
        if (mainClass != null) {
            logger.info("🎯 Executing main class: {}", mainClass);
            executeMainClass(mainClass);
        } else if (module != null) {
            logger.info("📚 Executing training module: {} with example: {}", module, example);
            executeTrainingModule(module, example);
        } else {
            logger.info("ℹ️  No specific training module specified");
            showAvailableModules();
        }
    }

    /**
     * Extract argument value from command line args
     */
    private String extractArgument(String[] args, String key) {
        for (String arg : args) {
            if (arg.startsWith("--" + key + "=")) {
                return arg.substring(("--" + key + "=").length());
            } else if (arg.startsWith("-D" + key + "=")) {
                return arg.substring(("-D" + key + "=").length());
            }
        }
        return null;
    }

    /**
     * Execute a specific main class (for backward compatibility)
     */
    private void executeMainClass(String className) {
        try {
            logger.info("🔄 Loading and executing class: {}", className);
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Method main = clazz.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
            logger.info("✅ Successfully executed: {}", className);
        } catch (Exception e) {
            logger.error("❌ Failed to execute class: {}", className, e);
        }
    }

    /**
     * Execute a specific training module
     */
    private void executeTrainingModule(String module, String example) {
        logger.info("🎓 Training Module Execution - Module: {}, Example: {}", module, example);
        
        // This will be enhanced as we convert the training modules to Spring components
        switch (module) {
            case "Day01Foundation":
                logger.info("📖 Day 1: Foundation - Kafka Fundamentals");
                break;
            case "Day02DataFlow":
                logger.info("📊 Day 2: Data Flow - Message Patterns");
                break;
            case "Day03Producers":
                logger.info("📤 Day 3: Producers - Publishing Messages");
                break;
            case "Day04Consumers":
                logger.info("📥 Day 4: Consumers - Processing Messages");
                break;
            case "Day05Streams":
                logger.info("🌊 Day 5: Streams - Stream Processing");
                break;
            case "Day06Schemas":
                logger.info("📋 Day 6: Schemas - Avro and Schema Registry");
                break;
            case "Day07Connect":
                logger.info("🔗 Day 7: Connect - External System Integration");
                break;
            case "Day08Advanced":
                logger.info("🚀 Day 8: Advanced - Security and Production");
                break;
            case "EventMart":
                logger.info("🎭 EventMart Progressive Project");
                break;
            default:
                logger.warn("⚠️  Unknown training module: {}", module);
                showAvailableModules();
        }
    }

    /**
     * Show available training modules
     */
    private void showAvailableModules() {
        logger.info("📚 Available Training Modules:");
        logger.info("  🔹 Day01Foundation - Kafka Fundamentals");
        logger.info("  🔹 Day02DataFlow - Message Patterns");
        logger.info("  🔹 Day03Producers - Publishing Messages");
        logger.info("  🔹 Day04Consumers - Processing Messages");
        logger.info("  🔹 Day05Streams - Stream Processing");
        logger.info("  🔹 Day06Schemas - Avro and Schema Registry");
        logger.info("  🔹 Day07Connect - External System Integration");
        logger.info("  🔹 Day08Advanced - Security and Production");
        logger.info("  🔹 EventMart - Progressive Project");
        logger.info("");
        logger.info("💡 Usage Examples:");
        logger.info("  java -jar target/kafka-training-java-1.0.0.jar --training.module=Day01Foundation");
        logger.info("  java -jar target/kafka-training-java-1.0.0.jar --exec.mainClass=com.training.kafka.Day01Foundation.BasicTopicOperations");
        logger.info("  java -jar target/kafka-training-java-1.0.0.jar (for web interface)");
    }
}
