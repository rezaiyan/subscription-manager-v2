#!/bin/bash

# Debug script for Subscription Manager logs
# Usage: ./debug-logs.sh [command] [pattern]

LOG_DIR="logs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Subscription Manager Debug Script ===${NC}"

case "$1" in
    "errors")
        echo -e "${RED}=== ERROR LOGS ===${NC}"
        echo -e "${YELLOW}Create Subscription Service Errors:${NC}"
        if [ -f "$LOG_DIR/create-subscription-service-error.log" ]; then
            tail -50 "$LOG_DIR/create-subscription-service-error.log"
        else
            echo "No error log file found"
        fi
        echo -e "\n${YELLOW}Main Server Errors:${NC}"
        if [ -f "$LOG_DIR/subscription-manager-error.log" ]; then
            tail -50 "$LOG_DIR/subscription-manager-error.log"
        else
            echo "No error log file found"
        fi
        ;;
    "create-service")
        echo -e "${GREEN}=== Create Subscription Service Logs ===${NC}"
        if [ -f "$LOG_DIR/create-subscription-service.log" ]; then
            if [ -n "$2" ]; then
                grep -i "$2" "$LOG_DIR/create-subscription-service.log" | tail -50
            else
                tail -50 "$LOG_DIR/create-subscription-service.log"
            fi
        else
            echo "No log file found"
        fi
        ;;
    "main-server")
        echo -e "${GREEN}=== Main Server Logs ===${NC}"
        if [ -f "$LOG_DIR/subscription-manager.log" ]; then
            if [ -n "$2" ]; then
                grep -i "$2" "$LOG_DIR/subscription-manager.log" | tail -50
            else
                tail -50 "$LOG_DIR/subscription-manager.log"
            fi
        else
            echo "No log file found"
        fi
        ;;
    "all")
        echo -e "${GREEN}=== All Logs ===${NC}"
        if [ -n "$2" ]; then
            echo -e "${YELLOW}Searching for '$2' in all log files:${NC}"
            find "$LOG_DIR" -name "*.log" -exec grep -l -i "$2" {} \; | while read file; do
                echo -e "\n${BLUE}=== $file ===${NC}"
                grep -i "$2" "$file" | tail -20
            done
        else
            echo -e "${YELLOW}Recent logs from all services:${NC}"
            for logfile in "$LOG_DIR"/*.log; do
                if [ -f "$logfile" ]; then
                    echo -e "\n${BLUE}=== $(basename "$logfile") ===${NC}"
                    tail -20 "$logfile"
                fi
            done
        fi
        ;;
    "post-requests")
        echo -e "${GREEN}=== POST Request Logs ===${NC}"
        echo -e "${YELLOW}Create Subscription Service POST requests:${NC}"
        if [ -f "$LOG_DIR/create-subscription-service.log" ]; then
            grep -i "POST /api/subscriptions" "$LOG_DIR/create-subscription-service.log" | tail -20
        fi
        echo -e "\n${YELLOW}Main Server POST requests:${NC}"
        if [ -f "$LOG_DIR/subscription-manager.log" ]; then
            grep -i "POST /api/subscriptions" "$LOG_DIR/subscription-manager.log" | tail -20
        fi
        ;;
    "kafka")
        echo -e "${GREEN}=== Kafka Event Logs ===${NC}"
        echo -e "${YELLOW}Kafka events in Create Service:${NC}"
        if [ -f "$LOG_DIR/create-subscription-service.log" ]; then
            grep -i "kafka\|event" "$LOG_DIR/create-subscription-service.log" | tail -20
        fi
        echo -e "\n${YELLOW}Kafka events in Main Server:${NC}"
        if [ -f "$LOG_DIR/subscription-manager.log" ]; then
            grep -i "kafka\|event" "$LOG_DIR/subscription-manager.log" | tail -20
        fi
        ;;
    "database")
        echo -e "${GREEN}=== Database Operation Logs ===${NC}"
        echo -e "${YELLOW}Database operations in Create Service:${NC}"
        if [ -f "$LOG_DIR/create-subscription-service.log" ]; then
            grep -i "save\|find\|database\|hibernate" "$LOG_DIR/create-subscription-service.log" | tail -20
        fi
        echo -e "\n${YELLOW}Database operations in Main Server:${NC}"
        if [ -f "$LOG_DIR/subscription-manager.log" ]; then
            grep -i "save\|find\|database\|hibernate" "$LOG_DIR/subscription-manager.log" | tail -20
        fi
        ;;
    "status")
        echo -e "${GREEN}=== Log File Status ===${NC}"
        for logfile in "$LOG_DIR"/*.log; do
            if [ -f "$logfile" ]; then
                size=$(du -h "$logfile" | cut -f1)
                lines=$(wc -l < "$logfile")
                echo -e "${BLUE}$(basename "$logfile"):${NC} $size, $lines lines"
            fi
        done
        ;;
    *)
        echo -e "${YELLOW}Usage:${NC}"
        echo "  $0 errors                    - Show recent errors from both services"
        echo "  $0 create-service [pattern]  - Show create service logs (optionally filtered)"
        echo "  $0 main-server [pattern]     - Show main server logs (optionally filtered)"
        echo "  $0 all [pattern]             - Show all logs (optionally filtered)"
        echo "  $0 post-requests             - Show POST request logs"
        echo "  $0 kafka                     - Show Kafka event logs"
        echo "  $0 database                  - Show database operation logs"
        echo "  $0 status                    - Show log file status"
        echo ""
        echo -e "${YELLOW}Examples:${NC}"
        echo "  $0 create-service 'POST'     - Show POST requests in create service"
        echo "  $0 all 'ERROR'               - Show all error messages"
        echo "  $0 all 'subscription'        - Show all subscription-related logs"
        ;;
esac 