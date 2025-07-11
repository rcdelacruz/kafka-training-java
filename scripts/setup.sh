#!/bin/bash

# Kafka Training Course Setup Script
# This script sets up the local environment for the Kafka training course

set -e

echo "🚀 Setting up Kafka Training Environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is installed
check_java() {
    print_status "Checking Java installation..."
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        print_status "Java version found: $JAVA_VERSION"
        
        # Check if Java 21 or higher
        MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d. -f1)
        if [ "$MAJOR_VERSION" -ge 21 ]; then
            print_status "Java version is compatible ✓"
        else
            print_warning "Java 21 or higher is recommended. Current version: $JAVA_VERSION"
        fi
    else
        print_error "Java not found! Please install Java 21 or higher"
        echo "  - macOS: brew install openjdk@21"
        echo "  - Ubuntu: sudo apt install openjdk-21-jdk"
        exit 1
    fi
}

# Check if Maven is installed
check_maven() {
    print_status "Checking Maven installation..."
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
        print_status "Maven version found: $MVN_VERSION ✓"
    else
        print_error "Maven not found! Please install Maven 3.8+"
        echo "  - macOS: brew install maven"
        echo "  - Ubuntu: sudo apt install maven"
        exit 1
    fi
}

# Check if Confluent CLI is installed
check_confluent_cli() {
    print_status "Checking Confluent CLI installation..."
    if command -v confluent &> /dev/null; then
        CLI_VERSION=$(confluent version | head -n 1)
        print_status "Confluent CLI found: $CLI_VERSION ✓"
    else
        print_warning "Confluent CLI not found. Installing..."
        curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest
        
        # Add to PATH for current session
        export PATH="$HOME/.confluent/bin:$PATH"
        
        print_status "Confluent CLI installed ✓"
        print_warning "Please add the following to your ~/.bashrc or ~/.zshrc:"
        echo "  export PATH=\"\$HOME/.confluent/bin:\$PATH\""
    fi
}

# Start Kafka services
start_kafka() {
    print_status "Starting Kafka services..."
    
    # Check if Kafka is already running
    if confluent local services list | grep -q "kafka.*Up"; then
        print_status "Kafka is already running ✓"
    else
        print_status "Starting Kafka cluster..."
        confluent local kafka start
        
        # Wait for services to be ready
        print_status "Waiting for services to be ready..."
        sleep 10
        
        # Verify services are running
        if confluent local services list | grep -q "kafka.*Up"; then
            print_status "Kafka started successfully ✓"
        else
            print_error "Failed to start Kafka services"
            exit 1
        fi
    fi
}

# Create training topics
create_topics() {
    print_status "Creating training topics..."
    
    TOPICS=(
        "my-first-topic:3:1"
        "user-events:6:1"
        "order-events:3:1"
        "session-events:3:1"
        "demo-topic-1:3:1"
        "demo-topic-2:6:1"
    )
    
    for topic_config in "${TOPICS[@]}"; do
        IFS=':' read -r topic partitions replication <<< "$topic_config"
        
        if confluent local kafka topic list | grep -q "^$topic$"; then
            print_status "Topic '$topic' already exists"
        else
            confluent local kafka topic create "$topic" \
                --partitions "$partitions" \
                --replication-factor "$replication"
            print_status "Created topic '$topic' with $partitions partitions ✓"
        fi
    done
}

# Compile Java project
compile_project() {
    print_status "Compiling Java project..."
    
    if [ -f "pom.xml" ]; then
        mvn clean compile
        print_status "Project compiled successfully ✓"
    else
        print_error "pom.xml not found! Are you in the correct directory?"
        exit 1
    fi
}

# Create logs directory
create_logs_dir() {
    print_status "Creating logs directory..."
    mkdir -p logs
    print_status "Logs directory created ✓"
}

# Display setup summary
display_summary() {
    echo
    echo "🎉 Setup completed successfully!"
    echo
    echo "📚 Training environment is ready:"
    echo "  • Kafka cluster running on localhost:9092"
    echo "  • Schema Registry running on localhost:8081"
    echo "  • Training topics created"
    echo "  • Java project compiled"
    echo
    echo "🚀 Next steps:"
    echo "  1. Start with: docs/day01-foundation.md"
    echo "  2. Run examples: mvn exec:java -Dexec.mainClass=\"...\""
    echo "  3. Check services: confluent local services list"
    echo
    echo "🛠 Useful commands:"
    echo "  • Stop Kafka: confluent local kafka stop"
    echo "  • View logs: confluent local services kafka log"
    echo "  • List topics: confluent local kafka topic list"
    echo
}

# Main execution
main() {
    echo "==============================================="
    echo "    Apache Kafka Training Course Setup"
    echo "==============================================="
    echo
    
    check_java
    check_maven
    check_confluent_cli
    start_kafka
    create_topics
    create_logs_dir
    compile_project
    display_summary
}

# Run main function
main "$@"