# Subscription Manager

A comprehensive subscription management system built with Kotlin Multiplatform and Spring Boot microservices.

## Project Structure

```
Subscription Manager/
├── 📁 server/                    # Backend services (consolidated)
│   ├── main-service/            # Main subscription manager service
│   ├── api-gateway/             # API Gateway service
│   ├── create-subscription-service/ # Subscription creation service
│   ├── eureka-server/           # Service discovery
│   └── config-server/           # Configuration server
├── 📁 composeApp/               # Kotlin Multiplatform frontend
├── 📁 shared/                   # Shared code between platforms
├── 📁 iosApp/                   # iOS-specific code
├── 📁 scripts/                  # Deployment and management scripts
│   ├── run-all.sh              # Start all services
│   ├── stop-all.sh             # Stop all services
│   ├── debug-logs.sh           # View service logs
│   ├── quick-start.sh          # Quick setup
│   └── deploy.sh               # Production deployment
├── 📁 config/                   # Configuration files
│   ├── docker-compose.yml      # Docker services configuration
│   ├── docker-compose.prod.yml # Production Docker config
│   ├── config.env.example      # Environment variables template
│   └── subscription-manager.service # Systemd service file
├── 📁 docs/                     # Documentation
│   ├── README.md               # This file
│   ├── DEPLOYMENT_GUIDE.md     # Deployment instructions
│   ├── MICROSERVICE_SETUP.md   # Microservice architecture
│   └── ...                     # Other documentation
├── 📁 test-data/               # Test data and fixtures
├── 📁 logs/                    # Service logs
├── 📁 database-setup/          # Database initialization scripts
└── 📁 monitoring/              # Monitoring and observability
```

## Quick Start

1. **Start all services:**
   ```bash
   ./scripts/run-all.sh
   ```

2. **Stop all services:**
   ```bash
   ./scripts/stop-all.sh
   ```

3. **View logs:**
   ```bash
   ./scripts/debug-logs.sh
   ```

## Services

- **Eureka Server**: http://localhost:8761 (Service Discovery)
- **Config Server**: http://localhost:8888 (Configuration Management)
- **Create Subscription Service**: http://localhost:3001 (Subscription Creation)
- **Main Server**: http://localhost:3000 (Main Application)
- **API Gateway**: http://localhost:8080 (API Gateway)
- **Frontend**: http://localhost:8081 (Web Application)

## Development

- **Backend**: Kotlin + Spring Boot + Spring Cloud
- **Frontend**: Kotlin Multiplatform (Compose Multiplatform)
- **Database**: PostgreSQL
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config

## Architecture

This project follows a microservices architecture with:
- Service discovery and registration
- Centralized configuration management
- Event-driven communication via Kafka
- API Gateway for routing and security
- Multi-platform frontend support 