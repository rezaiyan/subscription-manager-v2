#!/bin/bash

# Subscription Manager Microservice Deployment Script
# This script sets up and deploys the complete microservice architecture

set -e  # Exit on any error

# Configuration
DB_USER="ali.rezaiyan"
DB_PASSWORD=""
DB_HOST="localhost"
DB_PORT="5432"
MAIN_DB="subscription_main_db"
CREATE_DB="subscription_create_db"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
}

# Check if running as root
check_root() {
    if [[ $EUID -eq 0 ]]; then
        error "This script should not be run as root. Please run as a regular user."
    fi
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        error "Java is not installed. Please install Java 17 or higher."
    fi
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [[ $JAVA_VERSION -lt 17 ]]; then
        error "Java version $JAVA_VERSION is too old. Please install Java 17 or higher."
    fi
    
    log "Java version: $(java -version 2>&1 | head -n 1)"
    
    # Check if PostgreSQL is installed
    if ! command -v psql &> /dev/null; then
        error "PostgreSQL is not installed. Please install PostgreSQL."
    fi
    
    # Check if PostgreSQL is running
    if ! pg_isready -h $DB_HOST -p $DB_PORT &> /dev/null; then
        error "PostgreSQL is not running on $DB_HOST:$DB_PORT"
    fi
    
    log "PostgreSQL is running on $DB_HOST:$DB_PORT"
    
    # Check if Gradle is available
    if ! command -v ./gradlew &> /dev/null; then
        error "Gradle wrapper (gradlew) not found. Please run this script from the project root."
    fi
    
    log "All prerequisites are satisfied"
}

# Setup databases
setup_databases() {
    log "Setting up databases..."
    
    # Create main database
    if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $MAIN_DB; then
        warn "Database $MAIN_DB already exists"
    else
        log "Creating database $MAIN_DB..."
        createdb -h $DB_HOST -p $DB_PORT -U $DB_USER $MAIN_DB
    fi
    
    # Create create service database
    if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $CREATE_DB; then
        warn "Database $CREATE_DB already exists"
    else
        log "Creating database $CREATE_DB..."
        createdb -h $DB_HOST -p $DB_PORT -U $DB_USER $CREATE_DB
    fi
    
    log "Databases setup completed"
}

# Build the project
build_project() {
    log "Building the project..."
    
    # Clean and build all services
    ./gradlew clean build -x test
    
    if [ $? -eq 0 ]; then
        log "Project built successfully"
    else
        error "Project build failed"
    fi
}

# Start Eureka Server
start_eureka() {
    log "Starting Eureka Server..."
    
    # Check if Eureka is already running
    if curl -s http://localhost:8761 > /dev/null 2>&1; then
        warn "Eureka Server is already running on port 8761"
        return 0
    fi
    
    # Start Eureka in background
    nohup ./gradlew :eureka-server:bootRun > logs/eureka.log 2>&1 &
    EUREKA_PID=$!
    echo $EUREKA_PID > pids/eureka.pid
    
    # Wait for Eureka to start
    log "Waiting for Eureka Server to start..."
    for i in {1..30}; do
        if curl -s http://localhost:8761 > /dev/null 2>&1; then
            log "Eureka Server started successfully (PID: $EUREKA_PID)"
            return 0
        fi
        sleep 2
    done
    
    error "Eureka Server failed to start within 60 seconds"
}

# Start Create Subscription Service
start_create_service() {
    log "Starting Create Subscription Service..."
    
    # Check if service is already running
    if curl -s http://localhost:3001/api/subscriptions/health > /dev/null 2>&1; then
        warn "Create Subscription Service is already running on port 3001"
        return 0
    fi
    
    # Start service in background
    nohup ./gradlew :create-subscription-service:bootRun > logs/create-service.log 2>&1 &
    CREATE_PID=$!
    echo $CREATE_PID > pids/create-service.pid
    
    # Wait for service to start
    log "Waiting for Create Subscription Service to start..."
    for i in {1..30}; do
        if curl -s http://localhost:3001/api/subscriptions/health > /dev/null 2>&1; then
            log "Create Subscription Service started successfully (PID: $CREATE_PID)"
            return 0
        fi
        sleep 2
    done
    
    error "Create Subscription Service failed to start within 60 seconds"
}

# Start Main Application
start_main_app() {
    log "Starting Main Application..."
    
    # Check if app is already running
    if curl -s http://localhost:3000/api/subscriptions > /dev/null 2>&1; then
        warn "Main Application is already running on port 3000"
        return 0
    fi
    
    # Start app in background
    nohup ./gradlew :server:bootRun > logs/main-app.log 2>&1 &
    MAIN_PID=$!
    echo $MAIN_PID > pids/main-app.pid
    
    # Wait for app to start
    log "Waiting for Main Application to start..."
    for i in {1..30}; do
        if curl -s http://localhost:3000/api/subscriptions > /dev/null 2>&1; then
            log "Main Application started successfully (PID: $MAIN_PID)"
            return 0
        fi
        sleep 2
    done
    
    error "Main Application failed to start within 60 seconds"
}

# Health check
health_check() {
    log "Performing health checks..."
    
    # Check Eureka
    if curl -s http://localhost:8761 > /dev/null 2>&1; then
        log "✅ Eureka Server is healthy"
    else
        error "❌ Eureka Server is not responding"
    fi
    
    # Check Create Service
    if curl -s http://localhost:3001/api/subscriptions/health > /dev/null 2>&1; then
        log "✅ Create Subscription Service is healthy"
    else
        error "❌ Create Subscription Service is not responding"
    fi
    
    # Check Main App
    if curl -s http://localhost:3000/api/subscriptions > /dev/null 2>&1; then
        log "✅ Main Application is healthy"
    else
        error "❌ Main Application is not responding"
    fi
    
    # Check service registration
    if curl -s http://localhost:8761/eureka/apps | grep -q "CREATE-SUBSCRIPTION-SERVICE"; then
        log "✅ Create Subscription Service is registered with Eureka"
    else
        warn "⚠️  Create Subscription Service is not registered with Eureka"
    fi
    
    log "All health checks passed!"
}

# Test functionality
test_functionality() {
    log "Testing application functionality..."
    
    # Test create subscription through main app
    RESPONSE=$(curl -s -X POST http://localhost:3000/api/subscriptions \
        -H "Content-Type: application/json" \
        -d '{"name": "Deployment Test", "description": "Testing deployment", "amount": 9.99, "frequency": "MONTHLY"}' \
        2>/dev/null)
    
    if echo "$RESPONSE" | grep -q "id"; then
        log "✅ Create subscription functionality is working"
        SUB_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        log "Created subscription with ID: $SUB_ID"
    else
        warn "⚠️  Create subscription functionality test failed"
    fi
    
    # Test list subscriptions
    if curl -s http://localhost:3000/api/subscriptions | grep -q "id"; then
        log "✅ List subscriptions functionality is working"
    else
        warn "⚠️  List subscriptions functionality test failed"
    fi
}

# Create necessary directories
create_directories() {
    log "Creating necessary directories..."
    
    mkdir -p logs
    mkdir -p pids
    
    log "Directories created"
}

# Show service status
show_status() {
    log "Service Status:"
    echo "=================="
    
    # Eureka
    if [ -f pids/eureka.pid ] && kill -0 $(cat pids/eureka.pid) 2>/dev/null; then
        echo "✅ Eureka Server: Running (PID: $(cat pids/eureka.pid))"
    else
        echo "❌ Eureka Server: Not running"
    fi
    
    # Create Service
    if [ -f pids/create-service.pid ] && kill -0 $(cat pids/create-service.pid) 2>/dev/null; then
        echo "✅ Create Service: Running (PID: $(cat pids/create-service.pid))"
    else
        echo "❌ Create Service: Not running"
    fi
    
    # Main App
    if [ -f pids/main-app.pid ] && kill -0 $(cat pids/main-app.pid) 2>/dev/null; then
        echo "✅ Main App: Running (PID: $(cat pids/main-app.pid))"
    else
        echo "❌ Main App: Not running"
    fi
    
    echo ""
    echo "Service URLs:"
    echo "============="
    echo "Eureka Dashboard: http://localhost:8761"
    echo "Main Application: http://localhost:3000"
    echo "Create Service:   http://localhost:3001"
    echo ""
    echo "API Endpoints:"
    echo "=============="
    echo "List Subscriptions: GET http://localhost:3000/api/subscriptions"
    echo "Create Subscription: POST http://localhost:3000/api/subscriptions"
    echo "Health Check: GET http://localhost:3001/api/subscriptions/health"
}

# Main deployment function
deploy() {
    log "Starting deployment of Subscription Manager Microservice Architecture..."
    
    check_root
    create_directories
    check_prerequisites
    setup_databases
    build_project
    start_eureka
    start_create_service
    start_main_app
    health_check
    test_functionality
    
    log "Deployment completed successfully!"
    echo ""
    show_status
}

# Stop all services
stop_services() {
    log "Stopping all services..."
    
    for pid_file in pids/*.pid; do
        if [ -f "$pid_file" ]; then
            PID=$(cat "$pid_file")
            if kill -0 $PID 2>/dev/null; then
                log "Stopping process $PID..."
                kill $PID
                rm "$pid_file"
            fi
        fi
    done
    
    log "All services stopped"
}

# Show logs
show_logs() {
    echo "Available log files:"
    echo "==================="
    ls -la logs/
    echo ""
    echo "To view a specific log, use: tail -f logs/<logfile>"
}

# Main script logic
case "${1:-deploy}" in
    "deploy")
        deploy
        ;;
    "stop")
        stop_services
        ;;
    "status")
        show_status
        ;;
    "logs")
        show_logs
        ;;
    "health")
        health_check
        ;;
    "test")
        test_functionality
        ;;
    *)
        echo "Usage: $0 {deploy|stop|status|logs|health|test}"
        echo ""
        echo "Commands:"
        echo "  deploy  - Deploy all services (default)"
        echo "  stop    - Stop all services"
        echo "  status  - Show service status"
        echo "  logs    - Show available log files"
        echo "  health  - Perform health checks"
        echo "  test    - Test application functionality"
        exit 1
        ;;
esac 