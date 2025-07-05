#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_status "Stopping all Subscription Manager services..."

# Stop all containers
docker-compose down --remove-orphans

# Remove any dangling containers
docker container prune -f

# Remove any dangling networks
docker network prune -f

print_success "All services stopped and cleaned up"
print_status "To start services again, run: ./run-all.sh" 