# Subscription Manager v2

A comprehensive subscription management system built with **Kotlin Multiplatform** frontend and **Spring Boot microservices** backend. This project demonstrates modern microservice architecture with event-driven communication, service discovery, and multi-platform support.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Multiplatform │───▶│   API Gateway   │───▶│  Eureka Server  │
│   Frontend      │    │   (Port 8080)   │    │  (Port 8761)    │
│   (Web/iOS/     │    └─────────────────┘    └─────────────────┘
│   Android)      │             │
└─────────────────┘             ▼
                       ┌─────────────────┐
                       │ Config Server   │
                       │ (Port 8888)     │
                       └─────────────────┘
                                │
                                ▼
        ┌─────────────────────────────────────────────────┐
        │                                                 │
        ▼                                                 ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│Subscription Mgr │    │Create Service   │    │PostgreSQL Main  │
│(Port 3000)      │    │(Port 3001)      │    │(Port 5432)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                ▼
                       ┌─────────────────┐
                       │PostgreSQL Create│
                       │(Port 5433)      │
                       └─────────────────┘
```

## 🚀 Quick Start

### Option 1: Automated Deployment (Recommended)
```bash
# Clone the repository
git clone <repository-url>
cd subscription-manager-v2

# Quick start with automated deployment
./scripts/quick-start.sh
```

### Option 2: Manual Start
```bash
# Start all services
./scripts/run-all.sh

# Stop all services
./scripts/stop-all.sh

# View logs
./scripts/debug-logs.sh
```

### Option 3: Docker Compose
```bash
# Start with Docker
docker-compose up -d

# Check status
docker-compose ps
```

## 📋 Service Inventory

| Service | Port | Purpose | Features |
|---------|------|---------|----------|
| **API Gateway** | 8080 | Centralized routing | Load balancing, service discovery |
| **Eureka Server** | 8761 | Service discovery | Health monitoring, service registry |
| **Config Server** | 8888 | Configuration management | Centralized config, environment-specific settings |
| **Subscription Manager** | 3000 | Main application | CRUD operations, subscription management |
| **Create Service** | 3001 | Subscription creation | Event publishing, dedicated database |
| **PostgreSQL Main** | 5432 | Main database | Read/update/delete operations |
| **PostgreSQL Create** | 5433 | Create service DB | Write operations only |

## 🎯 Features

### Frontend (Kotlin Multiplatform)
- **Multi-platform Support**: Web, iOS, Android
- **Modern UI**: Material Design 3 with Compose Multiplatform
- **Real-time Updates**: Live subscription management
- **Responsive Design**: Works across all devices
- **Offline Support**: Local state management

### Backend (Spring Boot Microservices)
- **Microservice Architecture**: Decoupled services with clear boundaries
- **Event-Driven Communication**: Apache Kafka for async messaging
- **Service Discovery**: Netflix Eureka for dynamic service registration
- **Configuration Management**: Spring Cloud Config for centralized config
- **Database Per Service**: Separate databases for different concerns
- **Health Monitoring**: Comprehensive health checks and metrics

### Key Capabilities
- ✅ **Subscription Management**: Create, read, update, delete subscriptions
- ✅ **Multi-frequency Support**: Monthly and yearly billing cycles
- ✅ **Active/Inactive Toggle**: Enable/disable subscriptions
- ✅ **Cost Tracking**: Monthly and yearly total calculations
- ✅ **Event-Driven Sync**: Asynchronous data synchronization
- ✅ **Service Health Monitoring**: Real-time service status
- ✅ **Cross-platform UI**: Web, iOS, and Android support

## 🛠️ Technology Stack

### Frontend
- **Kotlin Multiplatform**: Shared business logic
- **Compose Multiplatform**: Modern declarative UI
- **Material Design 3**: Consistent design system
- **Koin**: Dependency injection
- **Kotlinx Serialization**: JSON handling

### Backend
- **Spring Boot**: Application framework
- **Spring Cloud**: Microservice patterns
- **Netflix Eureka**: Service discovery
- **Spring Cloud Config**: Configuration management
- **Apache Kafka**: Event streaming
- **PostgreSQL**: Primary database
- **Gradle**: Build system

### DevOps & Monitoring
- **Docker**: Containerization
- **Docker Compose**: Multi-service orchestration
- **Health Checks**: Service monitoring
- **Logging**: Structured logging with SLF4J

## 📊 API Endpoints

### Main Service (Port 3000)
```bash
GET    /api/subscriptions              # List all subscriptions
GET    /api/subscriptions/{id}         # Get subscription by ID
PUT    /api/subscriptions/{id}         # Update subscription
DELETE /api/subscriptions/{id}         # Delete subscription
PATCH  /api/subscriptions/{id}/toggle-active  # Toggle active status
```

### Create Service (Port 3001)
```bash
POST   /api/subscriptions              # Create new subscription
GET    /api/subscriptions/health       # Health check
```

### API Gateway (Port 8080)
```bash
# Routes all requests to appropriate services
GET    /api/subscriptions              # → Main Service
POST   /api/subscriptions              # → Create Service
PUT    /api/subscriptions/{id}         # → Main Service
DELETE /api/subscriptions/{id}         # → Main Service
```

## 🗄️ Data Model

### Subscription Entity
```kotlin
data class Subscription(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val frequency: SubscriptionFrequency, // MONTHLY or YEARLY
    val startDate: String?,
    val nextBillingDate: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val monthlyAmount: Double,
    val yearlyAmount: Double
)
```

### Event-Driven Architecture
```
Create Service → Saves to Create DB → Publishes Event → Main Service → Saves to Main DB
```

## 🔧 Development

### Prerequisites
- **Java**: OpenJDK 17 or higher
- **PostgreSQL**: 13 or higher
- **Docker**: 20.10+ (optional)
- **Kotlin**: 1.8+ (for frontend development)

### Local Development Setup
```bash
# 1. Clone repository
git clone <repository-url>
cd subscription-manager-v2

# 2. Start databases
docker-compose up -d postgres-main postgres-create

# 3. Start backend services
./scripts/run-all.sh

# 4. Start frontend (Web)
cd composeApp
./gradlew wasmJsBrowserDevelopmentRun

# 5. Start frontend (Android)
./gradlew androidApp:installDebug

# 6. Start frontend (iOS)
cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 14'
```

### Building the Project
```bash
# Build all services
./gradlew build

# Build frontend only
./gradlew composeApp:build

# Build backend only
./gradlew server:build
```

## 🚀 Deployment

### Production Deployment
```bash
# Automated deployment
./scripts/deploy.sh deploy

# Check status
./scripts/deploy.sh status

# View logs
./scripts/deploy.sh logs
```

### Docker Deployment
```bash
# Production deployment
docker-compose -f docker-compose.prod.yml up -d

# Development deployment
docker-compose up -d
```

## 📈 Monitoring & Health Checks

### Service Health Endpoints
- **API Gateway**: http://localhost:8080/actuator/health
- **Subscription Manager**: http://localhost:3000/actuator/health
- **Create Service**: http://localhost:3001/actuator/health
- **Config Server**: http://localhost:8888/actuator/health

### Service Discovery Dashboard
- **Eureka Dashboard**: http://localhost:8761

### Monitoring Dashboard
- **Web Monitoring**: http://localhost:8081 (Frontend monitoring)

## 🧪 Testing

### API Testing
```bash
# Create subscription
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"name": "Netflix", "amount": 15.99, "frequency": "MONTHLY"}'

# Get all subscriptions
curl http://localhost:8080/api/subscriptions

# Toggle subscription status
curl -X PATCH http://localhost:8080/api/subscriptions/1/toggle-active
```

### Service Discovery Testing
```bash
# Check Eureka dashboard
curl http://localhost:8761

# Check service health
curl http://localhost:3000/actuator/health
```

## 📚 Documentation

- **[Deployment Guide](docs/DEPLOYMENT_GUIDE.md)**: Comprehensive deployment instructions
- **[Microservice Architecture](docs/COMPLETE_MICROSERVICE_ARCHITECTURE.md)**: Detailed architecture documentation
- **[Microservice Setup](docs/MICROSERVICE_SETUP.md)**: Setup and configuration guide
- **[Koin Setup](docs/KOIN_SETUP.md)**: Dependency injection configuration
- **[Database Setup](docs/SEPARATE_DATABASES_SETUP.md)**: Database configuration guide

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Documentation**: Check the `docs/` directory
- **Quick Help**: Run `./scripts/debug-logs.sh` for troubleshooting

---

**Built with ❤️ using Kotlin Multiplatform and Spring Boot** 