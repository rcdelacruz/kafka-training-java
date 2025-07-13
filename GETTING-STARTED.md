# Getting Started with Kafka Training

Welcome to the Apache Kafka Training Course! This guide will help you get started quickly, regardless of your experience level.

## 🎯 Choose Your Path

### 👶 **Complete Beginner** (New to Kafka)
If you're new to Kafka or distributed systems:

1. **Quick Setup**
   ```bash
   git clone https://github.com/rcdelacruz/kafka-training-java.git
   cd kafka-training-java
   ./scripts/setup.sh
   ```

2. **Verify Everything Works**
   ```bash
   ./scripts/verify-setup.sh
   ```

3. **Start Learning**
   - Read: [Day 1 Documentation](./docs/day01-foundation.md)
   - Practice: [Day 1 Exercises](./exercises/day01-exercises.md)

### 🧑‍💻 **Experienced Developer** (Know Java, new to Kafka)
If you're comfortable with Java but new to Kafka:

1. **Quick Setup**
   ```bash
   git clone https://github.com/rcdelacruz/kafka-training-java.git
   cd kafka-training-java
   ./scripts/setup.sh
   ```

2. **Jump to Code**
   ```bash
   # Run the first example
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"
   ```

3. **Fast Track**
   - Skim: [Day 1 Concepts](./docs/day01-foundation.md)
   - Focus on: [Day 3 Producers](./docs/day03-producers.md) and [Day 4 Consumers](./docs/day04-consumers.md)

### 🚀 **Kafka User** (Want to deepen knowledge)
If you've used Kafka before but want to master it:

1. **Setup**
   ```bash
   git clone https://github.com/rcdelacruz/kafka-training-java.git
   cd kafka-training-java
   ./scripts/setup.sh
   ```

2. **Advanced Focus**
   - Jump to: [Day 5 Streams](./docs/day05-streams.md)
   - Deep dive: [Day 6 Schemas](./docs/day06-schemas.md)
   - Production: [Day 8 Advanced](./docs/day08-advanced.md)

## ⚡ Quick Verification

After setup, verify everything works:

```bash
# Check your environment
./scripts/verify-setup.sh

# Test basic functionality
mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"

# Check Kafka is running
confluent local services list
```

## 📚 Course Structure Overview

| Phase | Days | Focus | Time Investment |
|-------|------|-------|----------------|
| **Foundation** | 1-2 | Concepts & Setup | 6 hours |
| **Development** | 3-5 | Java Implementation | 9 hours |
| **Advanced** | 6-8 | Production Topics | 9 hours |

## 🛠 What You'll Build

Throughout the course, you'll build:

- **Day 1**: Topic management with AdminClient
- **Day 2**: Understanding data flow patterns
- **Day 3**: Reliable message producers
- **Day 4**: Scalable consumer applications
- **Day 5**: Real-time stream processing
- **Day 6**: Schema-managed data pipelines
- **Day 7**: Data integration with Connect
- **Day 8**: Production-ready configurations

## 🎯 Learning Approach

### Daily Structure
Each day follows this pattern:
1. **Theory** (1 hour) - Core concepts
2. **Examples** (1 hour) - Guided code walkthrough
3. **Practice** (1 hour) - Hands-on exercises

### Hands-On Philosophy
- **Run every example** - Don't just read the code
- **Experiment** - Try variations and see what happens
- **Break things** - Learn from errors and troubleshooting

## 🚨 Common Setup Issues

### Java Version
```bash
# Check Java version
java --version

# Should be 11 or higher
# If not, install: brew install openjdk@11 (macOS)
```

### Kafka Not Starting
```bash
# Check what's using port 9092
lsof -i :9092

# Restart Kafka
confluent local kafka stop
confluent local kafka start
```

### Maven Issues
```bash
# Clean and recompile
mvn clean compile

# Check Maven version
mvn --version
```

## 📞 Getting Help

### During Setup
1. Run `./scripts/verify-setup.sh` for diagnostics
2. Check the troubleshooting section in [README.md](./README.md)
3. Review error messages carefully

### During Learning
1. Check the documentation for each day
2. Review the exercise solutions
3. Experiment with the code examples

## 🎉 Ready to Start?

Choose your path above and begin your Kafka journey!

### Next Steps
1. **Complete setup** using your chosen path
2. **Verify environment** with the verification script
3. **Start Day 1** with [Foundation Documentation](./docs/day01-foundation.md)

---

💡 **Pro Tip**: Take your time with the fundamentals. Kafka concepts build on each other, so a solid foundation will make advanced topics much easier!
