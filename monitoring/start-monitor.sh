#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Subscription Manager - Monitoring Dashboard ===${NC}"

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}Python 3 is not installed. Please install Python 3 and try again.${NC}"
    exit 1
fi

# Check if pip is available
if ! command -v pip3 &> /dev/null; then
    echo -e "${RED}pip3 is not installed. Please install pip3 and try again.${NC}"
    exit 1
fi

# Install dependencies in virtual environment
echo -e "${BLUE}Setting up virtual environment...${NC}"
if [ ! -d "venv" ]; then
    python3 -m venv venv
fi
source venv/bin/activate
pip install -r requirements.txt

# Check if ports are available
if lsof -Pi :8082 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}Port 8082 is already in use. Stopping existing process...${NC}"
    lsof -ti:8082 | xargs kill -9 2>/dev/null || true
    sleep 2
fi

if lsof -Pi :8083 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}Port 8083 is already in use. Stopping existing process...${NC}"
    lsof -ti:8083 | xargs kill -9 2>/dev/null || true
    sleep 2
fi

# Start the health checker in background
echo -e "${GREEN}Starting health checker proxy...${NC}"
cd "$(dirname "$0")"
source venv/bin/activate
python health-checker.py > health-checker.log 2>&1 &
HEALTH_CHECKER_PID=$!
echo "Health Checker PID: $HEALTH_CHECKER_PID"

# Wait a moment for health checker to start
sleep 3

# Start the monitoring server
echo -e "${GREEN}Starting monitoring dashboard...${NC}"
source venv/bin/activate
python server.py 