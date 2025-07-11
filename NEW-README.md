# Apache Kafka Training Course with Java

![Kafka Training](https://img.shields.io/badge/Apache%20Kafka-Training-orange)
![Java](https://img.shields.io/badge/Java-21-blue)
![Confluent](https://img.shields.io/badge/Confluent-Platform-green)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 🎯 Overview

This comprehensive training course is designed to take you from zero to proficient with Apache Kafka using Java. The course follows the [Confluent Quickstart path](https://developer.confluent.io/quickstart/kafka-local/?build=apps) and provides hands-on experience with real-world scenarios.

## 📚 Course Structure

### Phase 1: Foundation and Setup (Days 1-2)
- Kafka fundamentals and architecture
- Local environment setup with Confluent CLI
- Basic topic operations and message flow

### Phase 2: Java Development (Days 3-5)
- Java Producer development
- Java Consumer implementation
- Stream processing patterns
- Error handling and monitoring

### Phase 3: Advanced Topics (Days 6-8)
- Schema management with Avro
- Kafka Connect integration
- Security and authentication
- Performance optimization and monitoring

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose (optional)
- Git

### Option 1: Automated Setup (Recommended)

```bash
# Clone the repository
git clone https://github.com/rcdelacruz/kafka-training-java.git
cd kafka-training-java

# Run the setup script
chmod +x scripts/setup.sh
./scripts/setup.sh
```

### Option 2: Manual Setup

```bash
# 1. Install Confluent CLI
curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest

# 2. Start Kafka locally
confluent local kafka start

# 3. Build the project
mvn clean compile

# 4. Create training topics
confluent local kafka topic create user-events --partitions 6 --replication-factor 1
confluent local kafka topic create order-events --partitions 3 --replication-factor 1
```

### Option 3: Docker Setup

```bash
# Start with Docker Compose
cd docker
docker-compose up -d

# Access Kafka UI at http://localhost:8080
# Access Control Center at http://localhost:9021
```

## 📖 Learning Path

| Day | Topic | Focus | Java Examples |
|-----|-------|-------|---------------|
| 1 | [Foundation](./docs/day01-foundation.md) | Kafka concepts, setup | `BasicTopicOperations.java` |
| 2 | Data Flow | Topics, producers, consumers | CLI exercises |
| 3 | Java Producers | Producer development | `SimpleProducer.java` |
| 4 | Java Consumers | Consumer implementation | `SimpleConsumer.java` |
| 5 | Stream Processing | Processing patterns | Kafka Streams examples |
| 6 | Schema Management | Avro, Schema Registry | Avro serialization |
| 7 | Integration | Kafka Connect | Connect examples |
| 8 | Advanced Topics | Security, monitoring | Production configs |

## 🛠 Project Structure

```
kafka-training-java/
├── src/main/java/com/training/kafka/
│   ├── Day01Foundation/          # AdminClient, topic operations
│   │   └── BasicTopicOperations.java
│   ├── Day02DataFlow/            # CLI exercises and concepts
│   ├── Day03Producers/           # Producer examples
│   │   └── SimpleProducer.java
│   ├── Day04Consumers/           # Consumer examples
│   │   └── SimpleConsumer.java
│   ├── Day05Streams/             # Kafka Streams processing
│   ├── Day06Schemas/             # Avro and Schema Registry
│   ├── Day07Connect/             # Kafka Connect examples
│   └── Day08Advanced/            # Security and monitoring
├── src/main/resources/
│   ├── application.properties    # Configuration
│   └── schemas/                  # Avro schemas
├── docker/
│   └── docker-compose.yml        # Complete Kafka stack
├── docs/
│   └── day01-foundation.md       # Day 1 documentation
├── scripts/
│   └── setup.sh                  # Automated setup
└── exercises/                    # Practice exercises
```

## 🎮 Running Examples

### Day 1: Basic Topic Operations
```bash
mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"
```

### Day 3: Producer Examples
```bash
mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.SimpleProducer"
```

### Day 4: Consumer Examples
```bash
# Basic consumer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day04Consumers.SimpleConsumer"

# Consumer group demo
mvn exec:java -Dexec.mainClass="com.training.kafka.Day04Consumers.SimpleConsumer" -Dexec.args="group-demo"
```

## 🔧 Development Environment

### Required Dependencies (Included in POM)
- Apache Kafka Clients 3.8.0
- Confluent Platform 7.7.0
- Avro 1.12.0
- Jackson for JSON processing
- SLF4J + Logback for logging
- JUnit 5 + TestContainers for testing

### IDE Setup
1. Import as Maven project
2. Set Java 21 as project SDK
3. Enable annotation processing for Avro
4. Run `mvn compile` to generate Avro classes

## 📊 Progress Tracking

- [x] Repository setup and basic structure
- [x] Maven configuration with Kafka dependencies
- [x] Day 1: Foundation examples and documentation
- [x] Day 3: Producer examples
- [x] Day 4: Consumer examples
- [ ] Day 2: Data Flow documentation
- [ ] Day 5: Stream processing examples
- [ ] Day 6: Schema management examples
- [ ] Day 7: Kafka Connect examples
- [ ] Day 8: Advanced topics examples

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests (requires Kafka running)
mvn verify -Pintegration-tests

# Run specific test
mvn test -Dtest=BasicTopicOperationsTest
```

## 🐳 Docker Commands

```bash
# Start all services
docker-compose -f docker/docker-compose.yml up -d

# Stop all services
docker-compose -f docker/docker-compose.yml down

# View logs
docker-compose -f docker/docker-compose.yml logs kafka

# Access Kafka container
docker exec -it kafka-training-broker bash
```

## 🔍 Monitoring

### Web UIs
- **Kafka UI**: http://localhost:8080 (lightweight, fast)
- **Control Center**: http://localhost:9021 (enterprise features)

### CLI Monitoring
```bash
# List topics
confluent local kafka topic list

# Check consumer groups
confluent local kafka consumer group list

# View service status
confluent local services list
```

## 🚨 Troubleshooting

### Common Issues

1. **Port conflicts**
   ```bash
   # Check what's using Kafka ports
   lsof -i :9092
   lsof -i :2181
   ```

2. **Java version issues**
   ```bash
   # Verify Java version
   java --version
   javac --version
   ```

3. **Maven compilation issues**
   ```bash
   # Clean and recompile
   mvn clean compile
   ```

4. **Kafka not starting**
   ```bash
   # Check logs
   confluent local services kafka log
   
   # Restart services
   confluent local kafka stop
   confluent local kafka start
   ```

## 🤝 Contributing

Improvements and suggestions are welcome! Please:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-example`
3. Make your changes and test them
4. Submit a pull request

## 📚 Additional Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Platform Documentation](https://docs.confluent.io/)
- [Kafka Java Client API](https://kafka.apache.org/documentation/#api)
- [Confluent Developer Portal](https://developer.confluent.io/)
- [Kafka Streams Documentation](https://kafka.apache.org/documentation/streams/)

## 📝 License

This training course is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

**Ronald DC**  
- GitHub: [@rcdelacruz](https://github.com/rcdelacruz)
- Email: rcdelacruz@gmail.com
- Blog: [https://rcdelacruz.github.io](https://rcdelacruz.github.io)
- Company: [@coderstudio-dev](https://github.com/coderstudio-dev)

---

## 🎯 Learning Objectives Summary

By completing this course, you will:

✅ **Understand Kafka Architecture** - Core concepts, brokers, topics, partitions  
✅ **Master Java Kafka Clients** - Producers, consumers, admin operations  
✅ **Implement Stream Processing** - Real-time data processing patterns  
✅ **Handle Schema Evolution** - Avro schemas and Schema Registry  
✅ **Configure Security** - Authentication, authorization, encryption  
✅ **Monitor and Optimize** - Performance tuning and operational best practices  

---

🚀 **Ready to start your Kafka journey?** Begin with [Day 1: Foundation](./docs/day01-foundation.md)!