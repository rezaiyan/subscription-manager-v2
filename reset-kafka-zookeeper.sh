#!/bin/bash

echo "=============================================="
echo "Kafka/Zookeeper Data Reset Script"
echo "=============================================="

echo "Stopping all containers..."
docker-compose down

echo "Removing Kafka and Zookeeper data volumes..."
docker volume rm subscriptionmanager_kafka_data subscriptionmanager_zookeeper_data 2>/dev/null || true

echo "Starting infrastructure services fresh..."
docker-compose up -d zookeeper kafka

echo "Waiting for Zookeeper to be healthy..."
sleep 10

echo "Waiting for Kafka to be healthy..."
sleep 30

echo "Checking service status..."
docker-compose ps zookeeper kafka

echo "=============================================="
echo "Reset complete! You can now run ./run-all.sh"
echo "==============================================" 