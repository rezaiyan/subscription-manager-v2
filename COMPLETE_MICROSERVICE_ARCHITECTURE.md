# Complete Microservice Architecture

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │───▶│   API Gateway   │───▶│  Eureka Server  │
│                 │    │   (Port 8080)   │    │  (Port 8761)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
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

## 📋 Service Inventory

### 1. **API Gateway** (Port 8080)
- **Purpose**: Centralized routing and load balancing
- **Routes**: 
  - `/api/subscriptions` → Subscription Manager (GET, PUT, DELETE)
  - `/api/subscriptions` → Create Service (POST)
  - `/eureka` → Eureka Dashboard
- **Features**: Service discovery integration, load balancing

### 2. **Eureka Server** (Port 8761)
- **Purpose**: Service discovery and registration
- **Dashboard**: http://localhost:8761
- **Features**: Health monitoring, service metadata

### 3. **Config Server** (Port 8888)
- **Purpose**: Centralized configuration management
- **Features**: Externalized configuration, environment-specific settings
- **Access**: http://localhost:8888

### 4. **Subscription Manager** (Port 3000)
- **Purpose**: Read, update, delete operations
- **Endpoints**:
  - `GET /api/subscriptions` - List all
  - `GET /api/subscriptions/{id}` - Get by ID
  - `PUT /api/subscriptions/{id}` - Update
  - `DELETE /api/subscriptions/{id}` - Delete
  - `PATCH /api/subscriptions/{id}/toggle-active` - Toggle status

### 5. **Create Subscription Service** (Port 3001)
- **Purpose**: Subscription creation only
- **Endpoints**:
  - `POST /api/subscriptions` - Create new subscription
  - `GET /api/subscriptions/health` - Health check

### 6. **PostgreSQL Main** (Port 5432)
- **Purpose**: Main application database for read/update/delete operations
- **Database**: subscription_main_db

### 7. **PostgreSQL Create** (Port 5433)
- **Purpose**: Create subscription service database
- **Database**: subscription_create_db

## 🚀 Deployment Options

### Option 1: Docker Compose (Recommended)
```bash
# Build all services
./gradlew build

# Start complete stack
docker-compose up -d

# Check status
docker-compose ps
```

### Option 2: Manual Deployment
```bash
# 1. Start PostgreSQL
# 2. Start Eureka Server
cd eureka-server && ./gradlew bootRun

# 3. Start Config Server
cd config-server && ./gradlew bootRun

# 4. Start Create Service
cd create-subscription-service && ./gradlew bootRun

# 5. Start Main Application
cd server && ./gradlew bootRun

# 6. Start API Gateway
cd api-gateway && ./gradlew bootRun
```

## 🔧 Configuration Management

### Config Server Setup
- **Local Development**: Uses classpath configuration
- **Production**: Can use Git repository or external storage
- **Profiles**: Environment-specific configurations

### Service Configuration
Each service can be configured via:
1. **Application Properties**: Default configuration
2. **Config Server**: Centralized configuration
3. **Environment Variables**: Runtime overrides

## 📊 Monitoring & Observability

### Health Checks
- **API Gateway**: http://localhost:8080/actuator/health
- **Subscription Manager**: http://localhost:3000/actuator/health
- **Create Service**: http://localhost:3001/actuator/health
- **Config Server**: http://localhost:8888/actuator/health

### Metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Jaeger**: http://localhost:16686

## 🔄 Service Communication

### Synchronous Communication
- **REST APIs**: Service-to-service calls
- **Load Balancing**: Automatic via Eureka
- **Circuit Breakers**: Fault tolerance with Resilience4j

### Configuration Flow
```
Config Server → Services (on startup)
Eureka Server → Services (registration)
API Gateway → Services (routing)
```

## 📊 Data Consistency

### Database Per Service Pattern
- **Create Service**: Owns `subscription_create_db` (Port 5433)
- **Main Service**: Owns `subscription_main_db` (Port 5432)
- **Event-Driven Sync**: Create service publishes events, main service listens and syncs
- **Eventual Consistency**: Data is synchronized asynchronously via events

### Event Flow
```
Create Service → Saves to Create DB → Publishes Event → Main Service → Saves to Main DB
```

## 🧪 Testing the Architecture

### 1. Service Discovery
```bash
# Check Eureka Dashboard
curl http://localhost:8761
```

### 2. API Gateway Routing
```bash
# Create subscription (routes to create service)
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "amount": 29.99, "frequency": "MONTHLY"}'

# Get subscriptions (routes to main app)
curl http://localhost:8080/api/subscriptions
```

### 3. Direct Service Access
```bash
# Direct to create service
curl -X POST http://localhost:3001/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"name": "Direct", "amount": 19.99, "frequency": "YEARLY"}'

# Direct to main app
curl http://localhost:3000/api/subscriptions
```

## 🔒 Security Considerations

### Current State
- **No Authentication**: Services are open (development)
- **CORS**: Configured for local development
- **Database**: No password (development)

### Production Recommendations
1. **API Gateway**: Add authentication/authorization
2. **Service-to-Service**: mTLS certificates
3. **Database**: Strong passwords and encryption
4. **Network**: VPC and security groups

## 📈 Scaling Strategy

### Horizontal Scaling
- **API Gateway**: Multiple instances behind load balancer
- **Services**: Scale independently based on load
- **Database**: Read replicas for read-heavy operations

### Vertical Scaling
- **Memory**: Adjust JVM heap sizes
- **CPU**: Optimize application performance
- **Database**: Optimize queries and indexes

## 🔄 Next Steps for Production

### 1. **Message Broker Integration**
- **RabbitMQ/Kafka**: Asynchronous communication
- **Event Sourcing**: Event-driven architecture

### 2. **Container Orchestration**
- **Kubernetes**: Production deployment
- **Service Mesh**: Istio for advanced networking

### 3. **Advanced Monitoring**
- **ELK Stack**: Centralized logging
- **Custom Dashboards**: Business metrics
- **Alerting**: Proactive monitoring

### 4. **Security Hardening**
- **OAuth2/JWT**: Authentication
- **API Keys**: Service-to-service security
- **Encryption**: Data at rest and in transit

## 🎯 Benefits Achieved

1. **✅ Service Isolation**: Each service has single responsibility
2. **✅ Independent Deployment**: Can deploy services separately
3. **✅ Fault Tolerance**: Circuit breakers and health checks
4. **✅ Scalability**: Can scale services independently
5. **✅ Centralized Configuration**: Config server for all settings
6. **✅ Service Discovery**: Automatic service registration
7. **✅ API Gateway**: Centralized routing and monitoring
8. **✅ Observability**: Health checks and metrics
9. **✅ Technology Flexibility**: Can use different tech stacks
10. **✅ Development Efficiency**: Clear service boundaries 