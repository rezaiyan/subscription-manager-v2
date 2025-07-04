#!/bin/bash

# Quick Start Script for Subscription Manager
# This script provides a one-command deployment

set -e  # Exit on any error

echo "🚀 Starting Subscription Manager Microservice Deployment..."

# Check if we're in the right directory
if [ ! -f "deploy.sh" ] || [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Required files not found."
    echo "   Please run this script from the Subscription Manager project root directory."
    echo "   Expected files: deploy.sh, build.gradle.kts"
    exit 1
fi

# Make deploy.sh executable
chmod +x deploy.sh

# Create necessary directories
mkdir -p logs pids

# Run deployment with timeout (fallback for systems without timeout command)
echo "📦 Deploying services..."
echo "⏱️  This may take a few minutes..."
if command -v timeout >/dev/null 2>&1; then
    if timeout 600 ./deploy.sh deploy; then
        DEPLOY_SUCCESS=true
    else
        DEPLOY_SUCCESS=false
    fi
else
    if ./deploy.sh deploy; then
        DEPLOY_SUCCESS=true
    else
        DEPLOY_SUCCESS=false
    fi
fi

if [ "$DEPLOY_SUCCESS" = true ]; then
    echo ""
    echo "🎉 Deployment completed!"
    echo ""
    echo "📊 Service Status:"
    ./deploy.sh status
else
    echo ""
    echo "❌ Deployment failed! Please check the logs:"
    echo "   ./deploy.sh logs"
    echo ""
    echo "🔧 Troubleshooting:"
    echo "   1. Check if PostgreSQL is running"
    echo "   2. Verify Java 17+ is installed"
    echo "   3. Ensure ports 3000, 3001, 8761 are available"
    echo "   4. Check the deployment guide: DEPLOYMENT_GUIDE.md"
    exit 1
fi

echo ""
echo "🔗 Quick Access URLs:"
echo "   Main Application: http://localhost:3000"
echo "   Eureka Dashboard: http://localhost:8761"
echo "   Create Service:   http://localhost:3001"
echo ""
echo "📝 Useful Commands:"
echo "   Check status:     ./deploy.sh status"
echo "   View logs:        ./deploy.sh logs"
echo "   Health check:     ./deploy.sh health"
echo "   Stop services:    ./deploy.sh stop"
echo ""
echo "✅ Ready to use!" 