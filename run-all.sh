#!/bin/bash

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
    
    # Build compose app (web only to avoid iOS/Android build issues)
    print_status "Building compose app (web only)..."
    ./gradlew :composeApp:clean :composeApp:compileKotlinWasmJs -x test
    if [ $? -ne 0 ]; then
        print_error "Failed to build compose app web components"
        exit 1
    fi
    
    print_success "All services built successfully"
}

# Function to stop existing containers
stop_existing() {
    print_status "Stopping existing containers..."
    docker-compose down --remove-orphans
    print_success "Existing containers stopped"
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

# Function to verify Kafka is working
verify_kafka() {
    print_status "Verifying Kafka is working..."
    
    # Wait a bit more for Kafka to fully initialize
    sleep 10
    
    # Try to create a test topic using docker exec
    if docker exec kafka kafka-topics --create --if-not-exists \
        --bootstrap-server localhost:9092 \
        --replication-factor 1 \
        --partitions 1 \
        --topic test-topic > /dev/null 2>&1; then
        print_success "Kafka is working properly"
        
        # Clean up test topic
        docker exec kafka kafka-topics --delete \
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
    
    # Wait for application services to be ready
    wait_for_healthy "create-subscription-service" 30
    wait_for_healthy "subscription-manager" 30
    wait_for_healthy "api-gateway" 20
    
    # Start frontend
    print_status "Starting frontend..."
    docker-compose up -d website
    
    # Wait for frontend to be ready
    wait_for_healthy "website" 15
    
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

# Main execution
main() {
    print_status "Starting Subscription Manager Services"
    echo "=============================================="
    
    # Check Docker
    check_docker
    
    # Build services
    build_services
    
    # Stop existing containers
    stop_existing
    
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