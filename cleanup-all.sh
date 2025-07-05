#!/bin/bash

echo "=============================================="
echo "Complete Docker Resources Cleanup Script"
echo "=============================================="

echo "Stopping all containers..."
docker-compose down

echo "Removing all containers with subscription-manager in name..."
docker ps -a --filter "name=subscription-manager" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=zookeeper" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=kafka" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=postgres" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=config-server" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=eureka-server" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=create-subscription-service" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=subscription-manager" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=api-gateway" --format "{{.ID}}" | xargs -r docker rm -f
docker ps -a --filter "name=website" --format "{{.ID}}" | xargs -r docker rm -f

echo "Removing all volumes with subscriptionmanager in name..."
docker volume ls | grep subscriptionmanager | awk '{print $2}' | xargs -r docker volume rm

echo "Removing all networks with subscription-manager in name..."
docker network ls | grep subscription-manager | awk '{print $1}' | xargs -r docker network rm

echo "Removing all images with subscription-manager in name..."
docker images | grep subscription-manager | awk '{print $3}' | xargs -r docker rmi -f

echo "Removing dangling images..."
docker image prune -f

echo "Removing dangling volumes..."
docker volume prune -f

echo "Removing dangling networks..."
docker network prune -f

echo "=============================================="
echo "Cleanup complete! All resources removed."
echo "You can now run ./run-all.sh for a fresh start."
echo "==============================================" 