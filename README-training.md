# Apache Kafka Training Course with Java

![Kafka Training](https://img.shields.io/badge/Apache%20Kafka-Training-orange)
![Java](https://img.shields.io/badge/Java-21-blue)
![Confluent](https://img.shields.io/badge/Confluent-Platform-green)

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
- Docker and Docker Compose
- Git

### Setup Instructions

1. **Clone the repository**
```bash
git clone https://github.com/rcdelacruz/kafka-training-java.git
cd kafka-training-java
```

2. **Install Confluent CLI**
```bash
curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest
```

3. **Start Kafka locally**
```bash
confluent local kafka start
```

4. **Build the project**
```bash
mvn clean compile
```

## 📖 Learning Path

| Day | Topic | Focus |
|-----|-------|-------|
| 1 | Foundation | Kafka concepts, setup |
| 2 | Data Flow | Topics, producers, consumers |
| 3 | Java Producers | Producer development |
| 4 | Java Consumers | Consumer implementation |
| 5 | Stream Processing | Processing patterns |
| 6 | Schema Management | Avro, Schema Registry |
| 7 | Integration | Kafka Connect |
| 8 | Advanced Topics | Security, monitoring |

## 🛠 Project Structure

```
kafka-training-java/
├── src/main/java/com/training/kafka/
│   ├── Day01Foundation/
│   ├── Day02DataFlow/
│   ├── Day03Producers/
│   ├── Day04Consumers/
│   ├── Day05Streams/
│   ├── Day06Schemas/
│   ├── Day07Connect/
│   └── Day08Advanced/
├── docs/
└── exercises/
```

## 🎮 Interactive Examples

Each day includes hands-on exercises:

- **Day 1**: Basic topic creation and CLI operations
- **Day 2**: Command-line producers and consumers
- **Day 3**: Java producer development with error handling
- **Day 4**: Java consumer groups and offset management
- **Day 5**: Stream processing applications
- **Day 6**: Schema evolution and compatibility
- **Day 7**: Source and sink connectors
- **Day 8**: Security configuration and monitoring

## 📊 Progress Tracking

- [ ] Day 1: Kafka Fundamentals
- [ ] Day 2: Data Flow Basics
- [ ] Day 3: Java Producer Development
- [ ] Day 4: Java Consumer Implementation
- [ ] Day 5: Stream Processing
- [ ] Day 6: Schema Management
- [ ] Day 7: Kafka Connect
- [ ] Day 8: Advanced Topics

## 👨‍💻 Author

**Ronald DC**  
- GitHub: [@rcdelacruz](https://github.com/rcdelacruz)
- Email: rcdelacruz@gmail.com
- Blog: [https://rcdelacruz.github.io](https://rcdelacruz.github.io)

---

🚀 **Ready to start your Kafka journey?** Begin with Day 1!
