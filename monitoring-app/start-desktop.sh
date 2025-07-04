#!/bin/bash

echo "Starting Subscription Manager Monitoring Desktop App..."
echo "======================================================"

# Build the project
echo "Building the project..."
./gradlew :monitoring-app:build

# Run the desktop application
echo "Starting desktop application..."
./gradlew :monitoring-app:run --args="desktop" 