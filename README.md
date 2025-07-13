# Apache Kafka Training Course with Java

![Kafka Training](https://img.shields.io/badge/Apache%20Kafka-Training-orange)
![Java](https://img.shields.io/badge/Java-11+-blue)
![Confluent](https://img.shields.io/badge/Confluent-Platform-green)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 🎯 Overview

This comprehensive 8-day training course takes you from zero to proficient with Apache Kafka using Java. Designed for developers of all levels, it provides hands-on experience with real-world scenarios and follows industry best practices.

### 🚀 **EventMart Progressive Project**
Build a complete **e-commerce event streaming platform** throughout the 8 days! Each day adds new functionality, culminating in a professional demo showcasing all Kafka concepts. Perfect for portfolios and job interviews.

### 🎯 **IMPORTANT: Choose Your Learning Path**
This training offers **two approaches** - choose based on your goal:

- **🎭 FOR DEMO & ASSESSMENT**: [EventMart Progressive Project](./EVENTMART-PROJECT-GUIDE.md) ← **RECOMMENDED**
- **📚 FOR CONCEPT STUDY**: [docs/](./docs/) and [exercises/](./exercises/) directories

👉 **[See Complete Learning Path Guide](./LEARNING-PATHS.md)**

## 📚 Course Structure

### Phase 1: Foundation (Days 1-2)
- **Day 1**: Kafka fundamentals, architecture, and setup
- **Day 2**: Data flow, partitioning, and message patterns

### Phase 2: Java Development (Days 3-5)
- **Day 3**: Java Producer development and patterns
- **Day 4**: Java Consumer implementation and groups
- **Day 5**: Stream processing with Kafka Streams

### Phase 3: Advanced Topics (Days 6-8)
- **Day 6**: Schema management with Avro
- **Day 7**: Kafka Connect integration
- **Day 8**: Security, monitoring, and production best practices

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.8+
- Git

### Setup (Choose One Option)

#### Option 1: Automated Setup (Recommended for Beginners)
```bash
git clone <this repo>
cd kafka-training-java
chmod +x scripts/setup.sh
./scripts/setup.sh
```

#### Option 2: Manual Setup (For Experienced Users)
```bash
# 1. Install Confluent CLI
curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest

# 2. Start Kafka locally
confluent local kafka start

# 3. Build the project
mvn clean compile

# 4. Verify setup
mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"
```

#### Option 3: Docker Setup (Alternative)
```bash
cd docker
docker-compose up -d
# Access Kafka UI at http://localhost:8080
```

## 📖 Learning Path

| Day | Topic | Java Examples | Documentation |
|-----|-------|---------------|---------------|
| 1 | [Foundation](./docs/day01-foundation.md) | `BasicTopicOperations.java` | [Exercises](./exercises/day01-exercises.md) |
| 2 | [Data Flow](./docs/day02-dataflow.md) | CLI + Concepts | [Exercises](./exercises/day02-exercises.md) |
| 3 | [Producers](./docs/day03-producers.md) | `SimpleProducer.java`, `AdvancedProducer.java` | [Exercises](./exercises/day03-exercises.md) |
| 4 | [Consumers](./docs/day04-consumers.md) | `SimpleConsumer.java` | [Exercises](./exercises/day04-exercises.md) |
| 5 | [Streams](./docs/day05-streams.md) | `StreamProcessor.java` | [Exercises](./exercises/day05-exercises.md) |
| 6 | [Schemas](./docs/day06-schemas.md) | `AvroProducer.java`, `AvroConsumer.java` | [Exercises](./exercises/day06-exercises.md) |
| 7 | [Connect](./docs/day07-connect.md) | `ConnectorManager.java` | [Exercises](./exercises/day07-exercises.md) |
| 8 | [Advanced](./docs/day08-advanced.md) | `SecurityConfig.java`, `MonitoringExample.java` | [Exercises](./exercises/day08-exercises.md) |

## 🛠 Project Structure

### 🎭 **EventMart Project** (For Demo & Assessment)
```
src/main/java/com/training/kafka/eventmart/
├── EventMartTopicManager.java    # Day 1: Topic architecture
├── events/EventMartEvents.java   # Day 2: Event schemas
├── producers/                    # Day 3: Producer services
├── consumers/                    # Day 4: Consumer services
├── streams/                      # Day 5: Stream processing
├── schemas/                      # Day 6: Schema management
├── connect/                      # Day 7: External integration
├── production/                   # Day 8: Production features
└── demo/EventMartDemoOrchestrator.java  # Final demo
```

### 📚 **Concept Learning** (For Study Reference)
```
kafka-training-java/
├── docs/                         # Day-by-day concept explanations
│   ├── day01-foundation.md       # Kafka fundamentals
│   ├── day02-dataflow.md         # Message patterns
│   └── ... (days 3-8)
├── exercises/                    # Practice exercises
│   ├── day01-exercises.md        # Hands-on practice
│   └── ... (days 2-8)
├── src/main/java/com/training/kafka/
│   ├── Day01Foundation/          # Concept examples
│   ├── Day02DataFlow/            # Individual demos
│   └── ... (days 3-8)
└── src/main/resources/           # Configuration and schemas
```

## 🎮 Running Examples

### EventMart Progressive Project (Recommended)
```bash
# Day 1: Create EventMart topic architecture
mvn exec:java -Dexec.mainClass="com.training.kafka.eventmart.EventMartTopicManager"

# Final Demo: Complete EventMart platform
mvn exec:java -Dexec.mainClass="com.training.kafka.eventmart.demo.EventMartDemoOrchestrator"
```

### Individual Day Examples
```bash
# Day 1: Basic topic operations
mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"

# Day 3: Producer examples
mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.SimpleProducer"

# Day 4: Consumer examples
mvn exec:java -Dexec.mainClass="com.training.kafka.Day04Consumers.SimpleConsumer"
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=BasicTopicOperationsTest

# Integration tests (requires Kafka running)
mvn verify -Pintegration-tests
```

## 🔧 Development Environment

### Dependencies (Included)
- Apache Kafka Clients 3.8.0
- Confluent Platform 7.7.0
- Avro 1.12.0
- Jackson for JSON processing
- SLF4J + Logback for logging
- JUnit 5 + TestContainers for testing

### IDE Setup
1. Import as Maven project
2. Set Java 11+ as project SDK
3. Run `mvn compile` to generate Avro classes
4. Enable annotation processing

## 🔍 Monitoring & Troubleshooting

### Web UIs (Docker setup)
- **Kafka UI**: http://localhost:8080
- **Control Center**: http://localhost:9021

### CLI Commands
```bash
# List topics
confluent local kafka topic list

# Check consumer groups
confluent local kafka consumer group list

# View service status
confluent local services list
```

### Common Issues
1. **Port conflicts**: Check `lsof -i :9092`
2. **Java version**: Verify with `java --version`
3. **Kafka not starting**: Check logs with `confluent local services kafka log`

## 🎯 Learning Objectives

By completing this course, you will:

✅ **Understand Kafka Architecture** - Core concepts, brokers, topics, partitions
✅ **Master Java Kafka Clients** - Producers, consumers, admin operations
✅ **Implement Stream Processing** - Real-time data processing patterns
✅ **Handle Schema Evolution** - Avro schemas and Schema Registry
✅ **Configure Security** - Authentication, authorization, encryption
✅ **Monitor and Optimize** - Performance tuning and operational best practices

## 📚 Additional Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Platform Documentation](https://docs.confluent.io/)
- [Confluent Developer Portal](https://developer.confluent.io/)

## 👨‍💻 Author

**Ronald DC**
- GitHub: [@rcdelacruz](https://github.com/rcdelacruz)
- Email: rcdelacruz@gmail.com

---

🚀 **Ready to start your Kafka journey?** Begin with [Day 1: Foundation](./docs/day01-foundation.md)!
