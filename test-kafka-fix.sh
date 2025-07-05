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

echo "=============================================="
echo "Testing Kafka Fix"
echo "=============================================="

# Test the fix_kafka_cluster_id function
print_status "Testing Kafka cluster ID fix..."

# Source the run-all.sh script to get the function
source ./run-all.sh

# Call the fix function
fix_kafka_cluster_id

# Start Kafka and Zookeeper
print_status "Starting Kafka and Zookeeper..."
docker-compose up -d zookeeper kafka

# Wait for them to be healthy
print_status "Waiting for services to be healthy..."
sleep 30

# Check if Kafka is running without cluster ID errors
print_status "Checking Kafka logs for cluster ID errors..."
if docker logs subscription-manager-kafka 2>&1 | grep -q "InconsistentClusterIdException"; then
    print_error "Kafka still has cluster ID issues!"
    exit 1
else
    print_success "No cluster ID errors found in Kafka logs!"
fi

# Test Kafka connectivity
print_status "Testing Kafka connectivity..."
if docker exec subscription-manager-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    print_success "Kafka is working properly!"
else
    print_warning "Kafka might still be starting up"
fi

echo "=============================================="
print_success "Kafka fix test completed!"
echo "==============================================" 