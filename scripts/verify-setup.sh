#!/bin/bash

# Kafka Training Environment Verification Script
# This script verifies that the training environment is properly set up

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Verification functions
verify_java() {
    print_header "Java Environment"

    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d. -f1)

        if [ "$MAJOR_VERSION" -ge 11 ]; then
            print_success "Java $JAVA_VERSION (compatible)"
        else
            print_warning "Java $JAVA_VERSION (require 11+)"
        fi
    else
        print_error "Java not found"
        return 1
    fi
}

verify_maven() {
    print_header "Maven Build Tool"

    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
        print_success "Maven $MVN_VERSION"
    else
        print_error "Maven not found"
        return 1
    fi
}

verify_confluent_cli() {
    print_header "Confluent CLI"

    if command -v confluent &> /dev/null; then
        CLI_VERSION=$(confluent version 2>/dev/null | head -n 1 || echo "Unknown version")
        print_success "Confluent CLI: $CLI_VERSION"
    else
        print_error "Confluent CLI not found"
        return 1
    fi
}

verify_kafka_services() {
    print_header "Kafka Services"

    if command -v confluent &> /dev/null; then
        # Check if services are running
        SERVICES_OUTPUT=$(confluent local services list 2>/dev/null || echo "")

        if echo "$SERVICES_OUTPUT" | grep -q "kafka.*Up"; then
            print_success "Kafka broker running"
        else
            print_error "Kafka broker not running"
            print_info "Run: confluent local kafka start"
            return 1
        fi

        if echo "$SERVICES_OUTPUT" | grep -q "schema-registry.*Up"; then
            print_success "Schema Registry running"
        else
            print_warning "Schema Registry not running (needed for Day 6+)"
        fi
    else
        print_error "Cannot check Kafka services (Confluent CLI not found)"
        return 1
    fi
}

verify_kafka_connectivity() {
    print_header "Kafka Connectivity"

    # Test basic connectivity to Kafka
    if command -v confluent &> /dev/null; then
        if confluent local kafka topic list &> /dev/null; then
            print_success "Can connect to Kafka broker"

            # Count topics
            TOPIC_COUNT=$(confluent local kafka topic list 2>/dev/null | wc -l)
            print_info "Found $TOPIC_COUNT topics"
        else
            print_error "Cannot connect to Kafka broker"
            return 1
        fi
    else
        print_error "Cannot test connectivity (Confluent CLI not found)"
        return 1
    fi
}

verify_project_compilation() {
    print_header "Project Compilation"

    if [ -f "pom.xml" ]; then
        if mvn compile -q &> /dev/null; then
            print_success "Project compiles successfully"
        else
            print_error "Project compilation failed"
            print_info "Run: mvn clean compile"
            return 1
        fi
    else
        print_error "pom.xml not found (wrong directory?)"
        return 1
    fi
}

verify_training_topics() {
    print_header "Training Topics"

    EXPECTED_TOPICS=(
        "my-first-topic"
        "user-events"
        "order-events"
        "session-events"
        "demo-topic-1"
        "demo-topic-2"
    )

    if command -v confluent &> /dev/null; then
        EXISTING_TOPICS=$(confluent local kafka topic list 2>/dev/null || echo "")

        for topic in "${EXPECTED_TOPICS[@]}"; do
            if echo "$EXISTING_TOPICS" | grep -q "^$topic$"; then
                print_success "Topic '$topic' exists"
            else
                print_warning "Topic '$topic' missing"
            fi
        done
    else
        print_error "Cannot check topics (Confluent CLI not found)"
        return 1
    fi
}

run_sample_test() {
    print_header "Sample Java Example"

    print_info "Testing Day 1 BasicTopicOperations..."
    if mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations" -q &> /dev/null; then
        print_success "Day 1 example runs successfully"
    else
        print_error "Day 1 example failed to run"
        print_info "Check: mvn exec:java -Dexec.mainClass=\"com.training.kafka.Day01Foundation.BasicTopicOperations\""
        return 1
    fi
}

display_summary() {
    echo
    print_header "Verification Summary"

    if [ $OVERALL_STATUS -eq 0 ]; then
        echo -e "${GREEN}🎉 Environment verification passed!${NC}"
        echo
        echo "Your Kafka training environment is ready:"
        echo "• All prerequisites are installed"
        echo "• Kafka services are running"
        echo "• Project compiles successfully"
        echo "• Sample examples work"
        echo
        echo "🚀 You're ready to start the training!"
        echo "   Begin with: docs/day01-foundation.md"
    else
        echo -e "${RED}❌ Environment verification failed!${NC}"
        echo
        echo "Please fix the issues above before starting the training."
        echo "If you need help, check the troubleshooting section in README.md"
    fi
    echo
}

# Main execution
main() {
    echo "==============================================="
    echo "    Kafka Training Environment Verification"
    echo "==============================================="
    echo

    OVERALL_STATUS=0

    verify_java || OVERALL_STATUS=1
    echo
    verify_maven || OVERALL_STATUS=1
    echo
    verify_confluent_cli || OVERALL_STATUS=1
    echo
    verify_kafka_services || OVERALL_STATUS=1
    echo
    verify_kafka_connectivity || OVERALL_STATUS=1
    echo
    verify_project_compilation || OVERALL_STATUS=1
    echo
    verify_training_topics || OVERALL_STATUS=1
    echo
    run_sample_test || OVERALL_STATUS=1
    echo

    display_summary

    exit $OVERALL_STATUS
}

# Run main function
main "$@"
