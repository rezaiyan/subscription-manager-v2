#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# List of Docker Compose service ports and their default mappings
DOCKER_PORTS=(8761 8888 3001 3000 8080)
DOCKER_SERVICES=(eureka-server config-server create-subscription-service subscription-manager api-gateway)
PORT_OVERRIDES=()
OVERRIDE_FILE="docker-compose.override.ports.yml"

# Function to find a free port
find_free_port() {
    local base_port=$1
    local port=$base_port
    while lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; do
        port=$((port+10000))
    done
    echo $port
}

# Initialize Docker Compose command (will be updated after local services start)
DOCKER_COMPOSE_CMD="docker-compose"

# Function to check if a port is available and kill processes if needed
check_port() {
    local port=$1
    local service_name=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}Port $port is in use. Stopping existing process...${NC}"
        
        # Try to kill the process gracefully first
        lsof -ti:$port | xargs kill 2>/dev/null || true
        sleep 3
        
        # If still running, force kill
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo -e "${YELLOW}Force killing process on port $port...${NC}"
            lsof -ti:$port | xargs kill -9 2>/dev/null || true
            sleep 2
        fi
        
        # Final check
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo -e "${RED}Failed to free port $port for $service_name${NC}"
            echo -e "${YELLOW}Process details: $(lsof -i :$port)${NC}"
            return 1
        else
            echo -e "${GREEN}Port $port freed for $service_name${NC}"
            return 0
        fi
    else
        echo -e "${GREEN}Port $port is available for $service_name${NC}"
        return 0
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${BLUE}Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            echo -e "${GREEN}$service_name is ready!${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}Attempt $attempt/$max_attempts: $service_name not ready yet...${NC}"
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}Timeout waiting for $service_name to be ready${NC}"
    return 1
}

# Function to kill background processes on script exit
cleanup() {
    echo -e "${YELLOW}Cleaning up background processes...${NC}"
    if [ ! -z "$EUREKA_PID" ]; then kill $EUREKA_PID 2>/dev/null || true; fi
    if [ ! -z "$CONFIG_PID" ]; then kill $CONFIG_PID 2>/dev/null || true; fi
    if [ ! -z "$CREATE_PID" ]; then kill $CREATE_PID 2>/dev/null || true; fi
    if [ ! -z "$SERVER_PID" ]; then kill $SERVER_PID 2>/dev/null || true; fi
    if [ ! -z "$WEBSITE_PID" ]; then kill $WEBSITE_PID 2>/dev/null || true; fi
    echo -e "${GREEN}Cleanup completed${NC}"
}

# Set up trap to cleanup on script exit
trap cleanup EXIT

echo -e "${BLUE}=== Subscription Manager - Complete Startup Script ===${NC}"

# Check if Docker is running, try to start if not
if ! docker info >/dev/null 2>&1; then
    echo -e "${YELLOW}Docker is not running. Attempting to start Docker Desktop...${NC}"
    open -a Docker
    echo -n "Waiting for Docker to start"
    max_attempts=30
    attempt=1
    while ! docker info >/dev/null 2>&1; do
        if [ $attempt -ge $max_attempts ]; then
            echo -e "\n${RED}Docker did not start after $((max_attempts*2)) seconds. Please start Docker Desktop manually and try again.${NC}"
            exit 1
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo -e "\n${GREEN}Docker is now running!${NC}"
else
    echo -e "${GREEN}Docker is running${NC}"
fi

# Check all required ports
echo -e "${BLUE}Checking port availability...${NC}"
port_issues=false

check_port 2181 "Zookeeper" || port_issues=true
check_port 9092 "Kafka" || port_issues=true
check_port 5432 "PostgreSQL Main" || port_issues=true
check_port 5433 "PostgreSQL Create" || port_issues=true
check_port 8761 "Eureka Server" || port_issues=true
check_port 8888 "Config Server" || port_issues=true
check_port 3001 "Create Subscription Service" || port_issues=true
check_port 3000 "Main Server" || port_issues=true
check_port 8080 "API Gateway" || port_issues=true

if [ "$port_issues" = true ]; then
    echo -e "${YELLOW}Some ports could not be freed. Continuing anyway...${NC}"
    echo -e "${YELLOW}Services may fail to start if ports remain occupied.${NC}"
    sleep 3
fi

# Stop and remove all running containers
echo -e "${BLUE}Stopping any running containers...${NC}"
$DOCKER_COMPOSE_CMD down

# Build Docker images (if needed)
echo -e "${BLUE}Building Docker images...${NC}"
$DOCKER_COMPOSE_CMD build

# Start infrastructure services first
echo -e "${BLUE}Starting infrastructure services...${NC}"
$DOCKER_COMPOSE_CMD up -d zookeeper postgres-main postgres-create kafka

# Wait for infrastructure to be ready
echo -e "${BLUE}Waiting for infrastructure services to be ready...${NC}"
sleep 10

# Start Eureka Server
echo -e "${BLUE}Starting Eureka Server...${NC}"
./gradlew :eureka-server:bootRun > logs/eureka-server.log 2>&1 &
EUREKA_PID=$!
echo "Eureka Server PID: $EUREKA_PID"

# Wait for Eureka to be ready
wait_for_service "http://localhost:8761" "Eureka Server"

# Start Config Server
echo -e "${BLUE}Starting Config Server...${NC}"
./gradlew :config-server:bootRun > logs/config-server.log 2>&1 &
CONFIG_PID=$!
echo "Config Server PID: $CONFIG_PID"

# Wait for Config Server to be ready
wait_for_service "http://localhost:8888/actuator/health" "Config Server"

# Start Create Subscription Service
echo -e "${BLUE}Starting Create Subscription Service...${NC}"
./gradlew :create-subscription-service:bootRun > logs/create-service.log 2>&1 &
CREATE_PID=$!
echo "Create Subscription Service PID: $CREATE_PID"

# Wait for Create Service to be ready
wait_for_service "http://localhost:3001/actuator/health" "Create Subscription Service"

# Start Main Subscription Manager Server
echo -e "${BLUE}Starting Main Subscription Manager Server...${NC}"
./gradlew :server:bootRun > logs/main-server.log 2>&1 &
SERVER_PID=$!
echo "Main Server PID: $SERVER_PID"

# Wait for Main Server to be ready
wait_for_service "http://localhost:3000/actuator/health" "Main Server"

# Check for port overlaps and prepare override file for Docker services
echo -e "${BLUE}Checking for Docker Compose port overlaps...${NC}"
OVERRIDE_NEEDED=false
cat > $OVERRIDE_FILE <<EOF
version: '3.8'
services:
EOF
for i in "${!DOCKER_PORTS[@]}"; do
    port=${DOCKER_PORTS[$i]}
    service=${DOCKER_SERVICES[$i]}
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        new_port=$(find_free_port $port)
        if [ "$new_port" != "$port" ]; then
            echo -e "${YELLOW}Port $port for $service is in use. Remapping Docker Compose $service to $new_port -> $port${NC}"
            OVERRIDE_NEEDED=true
            cat >> $OVERRIDE_FILE <<EOF
  $service:
    ports:
      - "$new_port:$port"
EOF
            PORT_OVERRIDES+=("$service:$new_port")
        fi
    fi
done

if [ "$OVERRIDE_NEEDED" = true ]; then
    echo -e "${YELLOW}Using $OVERRIDE_FILE for Docker Compose port overrides.${NC}"
    DOCKER_COMPOSE_CMD="docker-compose -f docker-compose.yml -f $OVERRIDE_FILE"
else
    rm -f $OVERRIDE_FILE
fi

# Start API Gateway
echo -e "${BLUE}Starting API Gateway...${NC}"
$DOCKER_COMPOSE_CMD up -d api-gateway

# Wait for API Gateway to be ready
wait_for_service "http://localhost:8080/actuator/health" "API Gateway"

# Start the website application (Compose Multiplatform)
echo -e "${BLUE}Starting Website Application...${NC}"
cd composeApp
./gradlew wasmJsBrowserDevelopmentRun > ../logs/website.log 2>&1 &
WEBSITE_PID=$!
cd ..
echo "Website PID: $WEBSITE_PID"

# Wait a moment for website to start
sleep 5

echo -e "${GREEN}=== All Services Started Successfully! ===${NC}"
echo -e "${GREEN}Services running:${NC}"
echo -e "  • Zookeeper: ${GREEN}localhost:2181${NC}"
echo -e "  • Kafka: ${GREEN}localhost:9092${NC}"
echo -e "  • PostgreSQL Main: ${GREEN}localhost:5432${NC}"
echo -e "  • PostgreSQL Create: ${GREEN}localhost:5433${NC}"
echo -e "  • Eureka Server: ${GREEN}http://localhost:8761${NC}"
echo -e "  • Config Server: ${GREEN}http://localhost:8888${NC}"
echo -e "  • Create Subscription Service: ${GREEN}http://localhost:3001${NC}"
echo -e "  • Main Server: ${GREEN}http://localhost:3000${NC}"
echo -e "  • API Gateway: ${GREEN}http://localhost:8080${NC}"
echo -e "  • Website: ${GREEN}http://localhost:8080 (via API Gateway)${NC}"

echo -e "${BLUE}Log files are available in the logs/ directory${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"

# Keep the script running and wait for user interrupt
wait 