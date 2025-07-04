#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Subscription Manager - Monitoring Dashboard (Kotlin) ===${NC}"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java is not installed. Please install Java 17 or later and try again.${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Java 17 or later is required. Current version: $JAVA_VERSION${NC}"
    exit 1
fi

echo -e "${GREEN}Java version: $(java -version 2>&1 | head -n 1)${NC}"

# Check if Gradle wrapper is available
if [ ! -f "../gradlew" ]; then
    echo -e "${RED}Gradle wrapper not found. Please run this script from the project root.${NC}"
    exit 1
fi

# Check if port is available
if lsof -Pi :8083 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}Port 8083 is already in use. Stopping existing process...${NC}"
    lsof -ti:8083 | xargs kill -9 2>/dev/null || true
    sleep 2
fi

# Build and run the monitoring app
echo -e "${GREEN}Building monitoring app...${NC}"
cd "$(dirname "$0")"
../gradlew :monitoring-app:build

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Please check the error messages above.${NC}"
    exit 1
fi

echo -e "${GREEN}Starting monitoring dashboard...${NC}"
echo -e "${BLUE}Dashboard URL: http://localhost:8083/dashboard${NC}"
echo -e "${BLUE}Health API: http://localhost:8083/health${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
echo "-" * 50

# Run the app
../gradlew :monitoring-app:run 