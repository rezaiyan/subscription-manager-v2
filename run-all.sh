#!/bin/bash

# Subscription Manager Services Deployment Script
# 
# Recent fixes (2025-07-05):
# - Fixed health checks: Changed from curl to TCP connection checks
# - Fixed Kafka/Zookeeper cluster ID issues with proper volume management
# - Added comprehensive cleanup functionality
# - Increased timeouts for Spring Boot application services
# - Added start-apps command for development workflow

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to build all services
build_services() {
    print_status "Building all services..."
    
    # Build server services (only the ones we need)
    print_status "Building server services..."
    ./gradlew :server:config-server:build :server:eureka-server:build :server:create-subscription-service:build :server:main-service:build :server:api-gateway:build -x test
    if [ $? -ne 0 ]; then
        print_error "Failed to build server services"
        exit 1
    fi
    
    # Verify JAR files exist
    print_status "Verifying JAR files exist..."
    local missing_jars=()
    
    # Check each service JAR
    if [ ! -f "server/config-server/build/libs/"*.jar ]; then
        missing_jars+=("config-server")
    fi
    if [ ! -f "server/eureka-server/build/libs/"*.jar ]; then
        missing_jars+=("eureka-server")
    fi
    if [ ! -f "server/create-subscription-service/build/libs/"*.jar ]; then
        missing_jars+=("create-subscription-service")
    fi
    if [ ! -f "server/main-service/build/libs/"*.jar ]; then
        missing_jars+=("main-service")
    fi
    if [ ! -f "server/api-gateway/build/libs/"*.jar ]; then
        missing_jars+=("api-gateway")
    fi
    
    if [ ${#missing_jars[@]} -gt 0 ]; then
        print_error "Missing JAR files for: ${missing_jars[*]}"
        print_error "Please check the build output above for errors"
        exit 1
    fi
    
    print_success "All JAR files verified"
    
    # Build compose app (web only to avoid iOS/Android build issues)
    print_status "Building compose app (web only)..."
    ./gradlew :composeApp:clean :composeApp:compileKotlinWasmJs -x test
    if [ $? -ne 0 ]; then
        print_error "Failed to build compose app web components"
        exit 1
    fi
    
    print_success "All services built successfully"
}

# Function to cleanup all resources
cleanup_all() {
    print_status "Performing complete cleanup of all resources..."
    
    # Stop all containers
    print_status "Stopping all containers..."
    docker-compose down --remove-orphans
    
    # Remove all containers with subscription-manager related names
    print_status "Removing all containers with subscription-manager in name..."
    docker ps -a --filter "name=subscription-manager" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=zookeeper" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=kafka" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=postgres" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=config-server" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=eureka-server" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=create-subscription-service" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=api-gateway" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=website" --format "{{.ID}}" | xargs -r docker rm -f
    # Also remove any old containers with generic names
    docker ps -a --filter "name=^zookeeper$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^kafka$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^postgres-main$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^postgres-create$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^config-server$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^eureka-server$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^create-subscription-service$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^subscription-manager$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^api-gateway$" --format "{{.ID}}" | xargs -r docker rm -f
    docker ps -a --filter "name=^website$" --format "{{.ID}}" | xargs -r docker rm -f
    
    # Remove all volumes with subscriptionmanager in name
    print_status "Removing all volumes with subscriptionmanager in name..."
    docker volume ls | grep subscriptionmanager | awk '{print $2}' | xargs -r docker volume rm
    
    # Remove Kafka and Zookeeper volumes specifically
    print_status "Removing Kafka and Zookeeper volumes..."
    docker volume rm subscription-manager-v2_kafka_data subscription-manager-v2_zookeeper_data 2>/dev/null || true
    docker volume rm subscription-manager-kafka-data subscription-manager-zookeeper-data 2>/dev/null || true
    docker volume rm subscription-manager-kafka-data-dev subscription-manager-zookeeper-data-dev 2>/dev/null || true
    
    # Remove any remaining Kafka/Zookeeper volumes (more aggressive)
    print_status "Removing any remaining Kafka/Zookeeper volumes..."
    docker volume ls | grep -E "(kafka|zookeeper)" | awk '{print $2}' | xargs -r docker volume rm 2>/dev/null || true
    
    # Force remove any volumes with kafka or zookeeper in the name
    docker volume ls | grep -i -E "(kafka|zookeeper)" | awk '{print $2}' | xargs -r docker volume rm 2>/dev/null || true
    
    # Remove all networks with subscription-manager in name
    print_status "Removing all networks with subscription-manager in name..."
    docker network ls | grep subscription-manager | awk '{print $1}' | xargs -r docker network rm
    
    # Remove all images with subscription-manager in name
    print_status "Removing all images with subscription-manager in name..."
    docker images | grep subscription-manager | awk '{print $3}' | xargs -r docker rmi -f
    
    # Remove dangling resources
    print_status "Removing dangling resources..."
    docker image prune -f
    docker volume prune -f
    docker network prune -f
    
    print_success "Complete cleanup finished"
}

# Function to wait for service to be healthy
wait_for_healthy() {
    local service_name=$1
    local max_attempts=${2:-30}
    local attempt=1
    
    print_status "Waiting for $service_name to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if docker-compose ps $service_name | grep -q "healthy"; then
            print_success "$service_name is healthy"
            return 0
        fi
        
        print_status "Attempt $attempt/$max_attempts: $service_name not ready yet..."
        sleep 10
        attempt=$((attempt + 1))
    done
    
    print_warning "$service_name did not become healthy in time, but continuing..."
    return 1
}

# Function to fix Kafka cluster ID issues
fix_kafka_cluster_id() {
    print_status "Fixing Kafka cluster ID issues..."
    
    # Stop Kafka and Zookeeper if running
    docker-compose stop kafka zookeeper 2>/dev/null || true
    
    # Remove all possible Kafka and Zookeeper volumes
    print_status "Removing Kafka and Zookeeper volumes..."
    docker volume rm subscription-manager-v2_kafka_data subscription-manager-v2_zookeeper_data 2>/dev/null || true
    docker volume rm subscription-manager-kafka-data subscription-manager-zookeeper-data 2>/dev/null || true
    docker volume rm subscription-manager-kafka-data-dev subscription-manager-zookeeper-data-dev 2>/dev/null || true
    
    # Remove any remaining Kafka/Zookeeper volumes
    docker volume ls | grep -E "(kafka|zookeeper)" | awk '{print $2}' | xargs -r docker volume rm 2>/dev/null || true
    
    # Clean up dangling volumes
    docker volume prune -f
    
    print_success "Kafka cluster ID fix completed"
}

# Function to verify Kafka is working
verify_kafka() {
    print_status "Verifying Kafka is working..."
    
    # Wait a bit more for Kafka to fully initialize
    sleep 10
    
    # Try to create a test topic using docker exec
    if docker exec subscription-manager-kafka kafka-topics --create --if-not-exists \
        --bootstrap-server localhost:9092 \
        --replication-factor 1 \
        --partitions 1 \
        --topic test-topic > /dev/null 2>&1; then
        print_success "Kafka is working properly"
        
        # Clean up test topic
        docker exec subscription-manager-kafka kafka-topics --delete \
            --bootstrap-server localhost:9092 \
            --topic test-topic > /dev/null 2>&1
        return 0
    else
        print_warning "Kafka health check passed but topic creation failed. This might be normal during startup."
        return 1
    fi
}

# Function to start services
start_services() {
    print_status "Starting all services with Docker Compose..."
    
    # Fix Kafka cluster ID issues before starting
    fix_kafka_cluster_id
    
    # Ensure JAR files exist before starting services
    print_status "Ensuring JAR files exist before starting services..."
    local missing_jars=()
    
    # Check each service JAR
    if [ ! -f "server/config-server/build/libs/"*.jar ]; then
        missing_jars+=("config-server")
    fi
    if [ ! -f "server/eureka-server/build/libs/"*.jar ]; then
        missing_jars+=("eureka-server")
    fi
    if [ ! -f "server/create-subscription-service/build/libs/"*.jar ]; then
        missing_jars+=("create-subscription-service")
    fi
    if [ ! -f "server/main-service/build/libs/"*.jar ]; then
        missing_jars+=("main-service")
    fi
    if [ ! -f "server/api-gateway/build/libs/"*.jar ]; then
        missing_jars+=("api-gateway")
    fi
    
    if [ ${#missing_jars[@]} -gt 0 ]; then
        print_warning "Missing JAR files for: ${missing_jars[*]}"
        print_status "Rebuilding missing services..."
        ./gradlew :server:config-server:build :server:eureka-server:build :server:create-subscription-service:build :server:main-service:build :server:api-gateway:build -x test
        if [ $? -ne 0 ]; then
            print_error "Failed to rebuild server services"
            exit 1
        fi
        print_success "Services rebuilt successfully"
    fi
    
    # Start infrastructure services first
    print_status "Starting infrastructure services (Zookeeper, Kafka, PostgreSQL)..."
    docker-compose up -d zookeeper kafka postgres-main postgres-create
    
    # Wait for Zookeeper to be ready
    wait_for_healthy "zookeeper" 20
    
    # Wait for Kafka to be ready (longer timeout)
    wait_for_healthy "kafka" 40
    
    # Verify Kafka is working
    verify_kafka
    
    # Wait for PostgreSQL databases to be ready
    wait_for_healthy "postgres-main" 15
    wait_for_healthy "postgres-create" 15
    
    # Start Spring Boot services
    print_status "Starting Spring Boot services..."
    docker-compose up -d config-server eureka-server
    
    # Wait for config and eureka to be ready
    wait_for_healthy "config-server" 20
    wait_for_healthy "eureka-server" 20
    
    # Start application services
    print_status "Starting application services..."
    docker-compose up -d create-subscription-service subscription-manager api-gateway
    
    # Wait for application services to be ready (longer timeouts for Spring Boot apps)
    wait_for_healthy "create-subscription-service" 45
    wait_for_healthy "subscription-manager" 45
    wait_for_healthy "api-gateway" 30
    
    # Start frontend
    print_status "Starting frontend..."
    docker-compose up -d website
    
    # Wait for frontend to be ready
    wait_for_healthy "website" 20
    
    print_success "All services started successfully"
}

# Function to show service status
show_status() {
    print_status "Service Status:"
    echo ""
    docker-compose ps
    echo ""
    
    print_status "Service URLs:"
    echo "  Eureka Server: http://localhost:8761"
    echo "  Config Server: http://localhost:8889"
    echo "  API Gateway: http://localhost:8080"
    echo "  Main Service: http://localhost:3000"
    echo "  Create Service: http://localhost:3001"
    echo "  Frontend: http://localhost:8081"
    echo "  Kafka: localhost:9092"
    echo "  PostgreSQL Main: localhost:5432"
    echo "  PostgreSQL Create: localhost:5433"
}

# Function to show logs
show_logs() {
    print_status "Showing logs for all services..."
    docker-compose logs -f
}

# Function to start just application services (for development)
start_app_services() {
    print_status "Starting application services only..."
    
    # Check if infrastructure is running
    if ! docker-compose ps | grep -q "subscription-manager-zookeeper.*healthy"; then
        print_error "Infrastructure services are not running. Please run './run-all.sh' first."
        exit 1
    fi
    
    # Start application services
    docker-compose up -d create-subscription-service subscription-manager api-gateway website
    
    # Wait for application services to be ready
    wait_for_healthy "create-subscription-service" 45
    wait_for_healthy "subscription-manager" 45
    wait_for_healthy "api-gateway" 30
    wait_for_healthy "website" 20
    
    print_success "Application services started successfully"
    show_status
}

# Main execution
main() {
    print_status "Starting Subscription Manager Services"
    echo "=============================================="
    
    # Check Docker
    check_docker
    
    # Build services
    build_services
    
    # Cleanup all resources
    cleanup_all
    
    # Start services
    start_services
    
    # Show status
    show_status
    
    print_success "All services are now running!"
    print_status "You can access the application at: http://localhost:8081"
    print_status "To view logs, run: ./run-all.sh logs"
    print_status "To stop all services, run: docker-compose down"
}

# Handle command line arguments
case "${1:-}" in
    "logs")
        show_logs
        ;;
    "status")
        show_status
        ;;
    "stop")
        print_status "Stopping all services..."
        docker-compose down
        print_success "All services stopped"
        ;;
    "restart")
        print_status "Restarting all services..."
        docker-compose restart
        print_success "All services restarted"
        ;;
    "build")
        build_services
        ;;
    "cleanup")
        cleanup_all
        ;;
    "start-apps")
        start_app_services
        ;;
    "fix-kafka")
        fix_kafka_cluster_id
        print_status "Starting Kafka and Zookeeper..."
        docker-compose up -d zookeeper kafka
        wait_for_healthy "zookeeper" 20
        wait_for_healthy "kafka" 40
        verify_kafka
        print_success "Kafka fix completed and verified"
        ;;
    "rebuild-jars")
        print_status "Rebuilding all JAR files..."
        ./gradlew :server:config-server:build :server:eureka-server:build :server:create-subscription-service:build :server:main-service:build :server:api-gateway:build -x test
        if [ $? -eq 0 ]; then
            print_success "All JAR files rebuilt successfully"
        else
            print_error "Failed to rebuild JAR files"
            exit 1
        fi
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  (no args)  Start all services"
        echo "  logs       Show logs for all services"
        echo "  status     Show service status and URLs"
        echo "  stop       Stop all services"
        echo "  restart    Restart all services"
        echo "  build      Build all services only"
        echo "  cleanup    Cleanup all Docker resources"
        echo "  start-apps Start application services only (requires infrastructure)"
        echo "  fix-kafka  Fix Kafka cluster ID issues and restart Kafka/Zookeeper"
        echo "  rebuild-jars Rebuild all JAR files"
        echo "  help       Show this help message"
        ;;
    "")
        main
        ;;
    *)
        print_error "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac 