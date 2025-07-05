#!/bin/bash

set -e

# =============================================================================
# CONFIGURATION
# =============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Ensure logs directory exists
mkdir -p logs

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

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
    if [ ! -z "$GATEWAY_PID" ]; then kill $GATEWAY_PID 2>/dev/null || true; fi
    echo -e "${GREEN}Cleanup completed${NC}"
}

# Set up trap to cleanup on script exit
trap cleanup EXIT

# =============================================================================
# DOCKER MANAGEMENT FUNCTIONS
# =============================================================================

# Function to check Kafka container state and handle accordingly
check_kafka_container() {
    local kafka_status
    kafka_status=$(docker ps -a --filter "name=^/kafka$" --format '{{.Status}}')
    if [ -z "$kafka_status" ]; then
        echo -e "${YELLOW}Kafka container does not exist. It will be created by Docker Compose.${NC}"
        return 0
    fi
    if [[ "$kafka_status" == Exited* || "$kafka_status" == "Created"* ]]; then
        echo -e "${YELLOW}Kafka container exists but is not running. Removing...${NC}"
        docker rm -f kafka
        return 0
    fi
    if [[ "$kafka_status" == Up* ]]; then
        # Check health if available
        kafka_health=$(docker inspect --format='{{.State.Health.Status}}' kafka 2>/dev/null || echo "none")
        if [ "$kafka_health" == "unhealthy" ]; then
            echo -e "${RED}Kafka container is unhealthy. Removing and restarting...${NC}"
            docker rm -f kafka
            return 0
        fi
        echo -e "${GREEN}Kafka container is already running and healthy.${NC}"
        return 0
    fi
    echo -e "${RED}Kafka container is in an unknown state: $kafka_status. Removing...${NC}"
    docker rm -f kafka
}

# Function to clean up Zookeeper state to prevent Kafka broker ID conflicts
cleanup_zookeeper_state() {
    echo -e "${BLUE}Cleaning up Zookeeper state to prevent Kafka broker conflicts...${NC}"
    
    # Wait for Zookeeper to be ready
    local max_attempts=30
    local attempt=1
    while [ $attempt -le $max_attempts ]; do
        if docker exec zookeeper sh -c "echo srvr | nc localhost 2181" | grep -q "Zookeeper version"; then
            echo -e "${GREEN}Zookeeper is ready for cleanup${NC}"
            break
        fi
        echo -e "${YELLOW}Attempt $attempt/$max_attempts: Waiting for Zookeeper to be ready...${NC}"
        sleep 2
        attempt=$((attempt + 1))
    done
    
    if [ $attempt -gt $max_attempts ]; then
        echo -e "${RED}Zookeeper did not become ready. Skipping cleanup.${NC}"
        return 1
    fi
    
    # Try to clean up Kafka broker registrations (skip if tools not available)
    echo -e "${YELLOW}Attempting to clean up Kafka broker registrations from Zookeeper...${NC}"
    
    # Check if zkCli.sh is available
    if docker exec zookeeper which zkCli.sh >/dev/null 2>&1; then
        echo -e "${GREEN}Using zkCli.sh for cleanup${NC}"
        docker exec zookeeper zkCli.sh -server localhost:2181 rmr /brokers 2>/dev/null || true
        docker exec zookeeper zkCli.sh -server localhost:2181 rmr /kafka 2>/dev/null || true
        docker exec zookeeper zkCli.sh -server localhost:2181 rmr /admin 2>/dev/null || true
        docker exec zookeeper zkCli.sh -server localhost:2181 rmr /config 2>/dev/null || true
        echo -e "${GREEN}Zookeeper cleanup completed using zkCli.sh${NC}"
    else
        echo -e "${YELLOW}zkCli.sh not available, skipping Zookeeper cleanup${NC}"
        echo -e "${YELLOW}This is normal for newer Zookeeper images. Kafka will handle conflicts automatically.${NC}"
    fi
}

# =============================================================================
# DATABASE MANAGEMENT FUNCTIONS
# =============================================================================

# Function to initialize PostgreSQL databases with comprehensive checks
initialize_databases() {
    echo -e "${BLUE}Initializing PostgreSQL databases with comprehensive checks...${NC}"
    
    # Wait for PostgreSQL containers to be ready
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}Waiting for PostgreSQL containers to be ready...${NC}"
    while [ $attempt -le $max_attempts ]; do
        if docker exec config-postgres-main-1 pg_isready -U ali.rezaiyan >/dev/null 2>&1 && \
           docker exec config-postgres-create-1 pg_isready -U ali.rezaiyan >/dev/null 2>&1; then
            echo -e "${GREEN}PostgreSQL containers are ready${NC}"
            break
        fi
        echo -e "${YELLOW}Attempt $attempt/$max_attempts: Waiting for PostgreSQL to be ready...${NC}"
        sleep 2
        attempt=$((attempt + 1))
    done
    
    if [ $attempt -gt $max_attempts ]; then
        echo -e "${RED}PostgreSQL containers did not become ready. Skipping database initialization.${NC}"
        return 1
    fi
    
    # Check and create main database
    echo -e "${YELLOW}Checking main database...${NC}"
    if ! docker exec config-postgres-main-1 psql -U ali.rezaiyan -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='subscription_main_db'" | grep -q 1; then
        echo -e "${YELLOW}Creating main database...${NC}"
        docker exec config-postgres-main-1 psql -U ali.rezaiyan -d postgres -c "CREATE DATABASE subscription_main_db;"
        echo -e "${GREEN}Main database created successfully${NC}"
    else
        echo -e "${GREEN}Main database already exists${NC}"
    fi
    
    # Check and create create database
    echo -e "${YELLOW}Checking create database...${NC}"
    if ! docker exec config-postgres-create-1 psql -U ali.rezaiyan -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='subscription_create_db'" | grep -q 1; then
        echo -e "${YELLOW}Creating create database...${NC}"
        docker exec config-postgres-create-1 psql -U ali.rezaiyan -d postgres -c "CREATE DATABASE subscription_create_db;"
        echo -e "${GREEN}Create database created successfully${NC}"
    else
        echo -e "${GREEN}Create database already exists${NC}"
    fi
    
    # Check and create tables in main database
    echo -e "${YELLOW}Checking tables in main database...${NC}"
    if ! docker exec config-postgres-main-1 psql -U ali.rezaiyan -d subscription_main_db -tAc "SELECT 1 FROM information_schema.tables WHERE table_name='subscription'" | grep -q 1; then
        echo -e "${YELLOW}Creating tables in main database...${NC}"
        docker exec -i config-postgres-main-1 psql -U ali.rezaiyan -d subscription_main_db < database-setup/init-main-db.sql
        echo -e "${GREEN}Main database tables created successfully${NC}"
    else
        echo -e "${GREEN}Main database tables already exist${NC}"
    fi
    
    # Check and create tables in create database
    echo -e "${YELLOW}Checking tables in create database...${NC}"
    if ! docker exec config-postgres-create-1 psql -U ali.rezaiyan -d subscription_create_db -tAc "SELECT 1 FROM information_schema.tables WHERE table_name='subscription'" | grep -q 1; then
        echo -e "${YELLOW}Creating tables in create database...${NC}"
        docker exec -i config-postgres-create-1 psql -U ali.rezaiyan -d subscription_create_db < database-setup/init-create-db.sql
        echo -e "${GREEN}Create database tables created successfully${NC}"
    else
        echo -e "${GREEN}Create database tables already exist${NC}"
    fi
    
    # Verify database connectivity
    echo -e "${YELLOW}Verifying database connectivity...${NC}"
    if docker exec config-postgres-main-1 psql -U ali.rezaiyan -d subscription_main_db -c "SELECT 1;" >/dev/null 2>&1; then
        echo -e "${GREEN}Main database connectivity verified${NC}"
    else
        echo -e "${RED}Main database connectivity failed${NC}"
        return 1
    fi
    
    if docker exec config-postgres-create-1 psql -U ali.rezaiyan -d subscription_create_db -c "SELECT 1;" >/dev/null 2>&1; then
        echo -e "${GREEN}Create database connectivity verified${NC}"
    else
        echo -e "${RED}Create database connectivity failed${NC}"
        return 1
    fi
    
    echo -e "${GREEN}Database initialization and verification completed successfully${NC}"
}

# =============================================================================
# MAIN SCRIPT EXECUTION
# =============================================================================

echo -e "${BLUE}=== Subscription Manager - Complete Startup Script ===${NC}"

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}Docker is not running. Please start Docker and try again.${NC}"
    echo -e "${YELLOW}You can start Docker with:${NC}"
    echo -e "  • Docker Desktop: Open Docker Desktop application"
    echo -e "  • Docker CLI: sudo systemctl start docker (Linux)"
    echo -e "  • Docker CLI: brew services start docker (macOS with Homebrew)"
    echo -e "  • Docker CLI: sudo service docker start (Ubuntu/Debian)"
    exit 1
else
    echo -e "${GREEN}Docker is running${NC}"
fi

# Additional check to ensure Docker is fully ready
echo -e "${BLUE}Verifying Docker is fully ready...${NC}"
max_attempts=30
attempt=1
while [ $attempt -le $max_attempts ]; do
    if docker version >/dev/null 2>&1 && docker ps >/dev/null 2>&1; then
        echo -e "${GREEN}Docker is fully ready!${NC}"
        break
    fi
    echo -e "${YELLOW}Attempt $attempt/$max_attempts: Docker not fully ready yet...${NC}"
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo -e "${RED}Docker did not become fully ready. Continuing anyway...${NC}"
fi

# Verify Docker Compose is available (try both standalone and plugin versions)
if ! docker-compose --version >/dev/null 2>&1 && ! docker compose version >/dev/null 2>&1; then
    echo -e "${RED}Docker Compose is not available. Please install Docker Compose and try again.${NC}"
    echo -e "${YELLOW}You can install Docker Compose with:${NC}"
    echo -e "  • Docker CLI plugin: docker compose (included with Docker 20.10+)"
    echo -e "  • Standalone: pip install docker-compose"
    echo -e "  • Package manager: brew install docker-compose (macOS)"
    exit 1
fi

# Use docker compose (plugin) if available, otherwise fall back to docker-compose (standalone)
if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE="docker compose"
    echo -e "${GREEN}Using Docker Compose plugin${NC}"
else
    DOCKER_COMPOSE="docker-compose"
    echo -e "${GREEN}Using Docker Compose standalone${NC}"
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

# Stop only our specific services if they're running, without affecting other Docker containers
echo -e "${BLUE}Stopping any running subscription manager containers...${NC}"
if $DOCKER_COMPOSE -f config/docker-compose.yml ps | grep -q "subscription-manager\|api-gateway\|eureka-server\|config-server\|create-subscription-service"; then
    $DOCKER_COMPOSE -f config/docker-compose.yml stop api-gateway subscription-manager eureka-server config-server create-subscription-service zookeeper postgres-main postgres-create kafka 2>/dev/null || true
    $DOCKER_COMPOSE -f config/docker-compose.yml rm -f api-gateway subscription-manager eureka-server config-server create-subscription-service zookeeper postgres-main postgres-create kafka 2>/dev/null || true
else
    echo -e "${GREEN}No subscription manager containers running${NC}"
fi

# Build Docker images (if needed)
echo -e "${BLUE}Building Docker images...${NC}"
$DOCKER_COMPOSE -f config/docker-compose.yml build api-gateway subscription-manager eureka-server config-server create-subscription-service zookeeper postgres-main postgres-create kafka 2>/dev/null || true

# Check Kafka container state
check_kafka_container

# DEVELOPMENT ONLY: Always start with a clean Zookeeper/Kafka state (wipes all data)
echo -e "${YELLOW}Wiping all Docker containers and volumes for a clean state (development only)...${NC}"
$DOCKER_COMPOSE -f config/docker-compose.yml down -v

# Start infrastructure services first
echo -e "${BLUE}Starting infrastructure services...${NC}"
$DOCKER_COMPOSE -f config/docker-compose.yml up -d zookeeper postgres-main postgres-create

# Wait for PostgreSQL to be ready and initialize databases
sleep 5
initialize_databases

# Wait for Zookeeper to be ready and clean up any stale state
sleep 5
cleanup_zookeeper_state

# Now start Kafka after cleanup
echo -e "${BLUE}Starting Kafka...${NC}"
$DOCKER_COMPOSE -f config/docker-compose.yml up -d kafka

# Wait for infrastructure to be ready
echo -e "${BLUE}Waiting for infrastructure services to be ready...${NC}"
sleep 10

# Wait for Kafka to be fully ready
echo -e "${BLUE}Waiting for Kafka to be fully ready...${NC}"
max_attempts=30
attempt=1
while [ $attempt -le $max_attempts ]; do
    if docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
        echo -e "${GREEN}Kafka is fully ready!${NC}"
        break
    fi
    
    echo -e "${YELLOW}Attempt $attempt/$max_attempts: Kafka not ready yet...${NC}"
    sleep 3
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo -e "${RED}Kafka did not become ready in time. Continuing anyway...${NC}"
fi

# Start Eureka Server
echo -e "${BLUE}Starting Eureka Server...${NC}"
./gradlew :server:eureka-server:bootRun > logs/eureka-server.log 2>&1 &
EUREKA_PID=$!
echo "Eureka Server PID: $EUREKA_PID"

# Wait for Eureka to be ready
wait_for_service "http://localhost:8761" "Eureka Server"

# Start Config Server
echo -e "${BLUE}Starting Config Server...${NC}"
./gradlew :server:config-server:bootRun > logs/config-server.log 2>&1 &
CONFIG_PID=$!
echo "Config Server PID: $CONFIG_PID"

# Wait for Config Server to be ready
wait_for_service "http://localhost:8888/" "Config Server"

# Start Create Subscription Service
echo -e "${BLUE}Starting Create Subscription Service...${NC}"
./gradlew :server:create-subscription-service:bootRun > logs/create-service.log 2>&1 &
CREATE_PID=$!
echo "Create Subscription Service PID: $CREATE_PID"

# Wait for Create Service to be ready
wait_for_service "http://localhost:3001/actuator/health" "Create Subscription Service"

# Start Main Subscription Manager Server
echo -e "${BLUE}Starting Main Subscription Manager Server...${NC}"
./gradlew :server:main-service:bootRun > logs/main-server.log 2>&1 &
SERVER_PID=$!
echo "Main Server PID: $SERVER_PID"

# Wait for Main Server to be ready
wait_for_service "http://localhost:3000/actuator/health" "Main Server"

# Start API Gateway
echo -e "${BLUE}Starting API Gateway...${NC}"
./gradlew :server:api-gateway:bootRun > logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
echo "API Gateway PID: $GATEWAY_PID"

# Wait for API Gateway to be ready
wait_for_service "http://localhost:8080/actuator/health" "API Gateway"

# Website application should be started separately
# echo -e "${BLUE}Starting Website Application...${NC}"
# cd composeApp
# ../gradlew wasmJsBrowserDevelopmentRun > ../logs/website.log 2>&1 &
# WEBSITE_PID=$!
# cd ..
# echo "Website PID: $WEBSITE_PID"

# Wait a moment for services to start
sleep 5

# =============================================================================
# FINAL STATUS
# =============================================================================

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
echo -e "${YELLOW}Note: Website should be started separately with: cd composeApp && ../gradlew wasmJsBrowserDevelopmentRun${NC}"

echo -e "${BLUE}Log files are available in the logs/ directory${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"

# Keep the script running and wait for user interrupt
wait 