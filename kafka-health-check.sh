#!/bin/bash

# Kafka health check script
# This script checks if Kafka is ready to accept connections

# Function to check if port is open
check_port() {
    local host=$1
    local port=$2
    local timeout=${3:-5}
    
    # Try to connect to the port
    timeout $timeout bash -c "</dev/tcp/$host/$port" 2>/dev/null
    return $?
}

# Function to check Kafka broker
check_kafka_broker() {
    local host="localhost"
    local port="9092"
    
    # Check if port is open
    if ! check_port $host $port; then
        echo "Kafka port $port is not open"
        return 1
    fi
    
    # Try to get broker metadata (this is a more reliable check)
    # We'll use netcat to send a simple request
    echo "Checking Kafka broker at $host:$port..."
    
    # Simple check - if we can connect to the port, Kafka is likely ready
    # The actual Kafka protocol check would be more complex
    return 0
}

# Main health check
main() {
    echo "Starting Kafka health check..."
    
    # Wait a bit for Kafka to start
    sleep 5
    
    # Check Kafka broker
    if check_kafka_broker; then
        echo "Kafka is healthy"
        exit 0
    else
        echo "Kafka is not healthy"
        exit 1
    fi
}

# Run the health check
main "$@" 