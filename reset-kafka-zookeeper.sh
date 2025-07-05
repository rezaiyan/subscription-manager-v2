#!/bin/bash

echo "=============================================="
echo "Kafka/Zookeeper Data Reset Script"
echo "=============================================="

echo "Stopping all containers..."
docker-compose down

echo "Removing all Kafka and Zookeeper data volumes..."
docker volume rm subscriptionmanager_kafka_data subscriptionmanager_zookeeper_data 2>/dev/null || true

echo "Forcing removal of any remaining volumes..."
docker volume ls | grep -E "(kafka|zookeeper)" | awk '{print $2}' | xargs -r docker volume rm 2>/dev/null || true

echo "Starting infrastructure services fresh..."
docker-compose up -d zookeeper kafka

echo "Waiting for Zookeeper to be healthy..."
sleep 15

echo "Waiting for Kafka to be healthy..."
sleep 45

echo "Checking service status..."
docker-compose ps zookeeper kafka

echo "=============================================="
echo "Reset complete! You can now run ./run-all.sh"
echo "==============================================" 