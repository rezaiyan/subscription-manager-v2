#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Stopping All Subscription Manager Services ===${NC}"

# Stop Docker containers
echo -e "${BLUE}Stopping Docker containers...${NC}"
docker-compose -f config/docker-compose.yml down

# Kill Gradle processes
echo -e "${BLUE}Stopping Gradle processes...${NC}"
pkill -f "gradle.*bootRun" || true

# Kill any remaining Java processes related to our services
echo -e "${BLUE}Stopping Java processes...${NC}"
pkill -f "com.github.rezaiyan.subscriptionmanager" || true

# Kill any processes on our specific ports
echo -e "${BLUE}Stopping processes on service ports...${NC}"
for port in 8761 8888 3001 3000 8080 8081; do
    lsof -ti:$port | xargs kill -9 2>/dev/null || true
done

echo -e "${GREEN}All services stopped successfully!${NC}" 