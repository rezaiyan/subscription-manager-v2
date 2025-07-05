#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_status "Testing Kafka functionality..."

# Check if Kafka container is running
if ! docker ps | grep -q kafka; then
    print_error "Kafka container is not running"
    exit 1
fi

# Check if Kafka is listening on port 9092
if ! timeout 5 bash -c "</dev/tcp/localhost/9092" 2>/dev/null; then
    print_error "Kafka is not listening on port 9092"
    exit 1
fi

print_success "Kafka is listening on port 9092"

# Try to create a test topic
print_status "Creating test topic..."
if docker exec kafka kafka-topics --create --if-not-exists \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic kafka-test-topic > /dev/null 2>&1; then
    print_success "Test topic created successfully"
else
    print_error "Failed to create test topic"
    exit 1
fi

# List topics
print_status "Listing topics..."
if docker exec kafka kafka-topics --list \
    --bootstrap-server localhost:9092 | grep -q kafka-test-topic; then
    print_success "Test topic found in topic list"
else
    print_error "Test topic not found in topic list"
    exit 1
fi

# Clean up test topic
print_status "Cleaning up test topic..."
docker exec kafka kafka-topics --delete \
    --bootstrap-server localhost:9092 \
    --topic kafka-test-topic > /dev/null 2>&1

print_success "Kafka is working properly!"
print_status "All tests passed" 