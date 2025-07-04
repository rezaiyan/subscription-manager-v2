# Microservice Setup for Subscription Manager

This document explains how to run the microservice version of the Subscription Manager application.

## Architecture Overview

We have successfully extracted the **Create Subscription** functionality into a separate microservice while keeping all other operations in the main application. This is a step-by-step approach to microservices.

### Services

1. **Eureka Server** (Port 8761) - Service Discovery
2. **Create Subscription Service** (Port 3001) - Handles subscription creation
3. **Subscription Manager** (Port 3000) - Main application with all other operations
4. **PostgreSQL** (Port 5432) - Shared database

## How It Works

### Before (Monolithic)
```
Client → Subscription Manager → Database
```

### After (Microservice)
```
Client → Subscription Manager → Create Subscription Service → Database
                    ↓
                Database (for read/update/delete operations)
```

**Note**: The main application no longer handles subscription creation directly. All create operations are delegated to the microservice.

## Running the Application

### Option 1: Using Docker Compose (Recommended)

1. **Build all services:**
   ```bash
   ./gradlew build
   ```

2. **Start all services:**
   ```bash
   docker-compose up -d
   ```

3. **Check service status:**
   - Eureka Dashboard: http://localhost:8761
   - Main App: http://localhost:3000
   - Create Service: http://localhost:3001

### Option 2: Running Locally

1. **Start PostgreSQL:**
   ```bash
   # Make sure PostgreSQL is running on localhost:5432
   ```

2. **Start Eureka Server:**
   ```bash
   cd eureka-server
   ./gradlew bootRun
   ```

3. **Start Create Subscription Service:**
   ```bash
   cd create-subscription-service
   ./gradlew bootRun
   ```

4. **Start Main Application:**
   ```bash
   cd server
   ./gradlew bootRun
   ```

## API Endpoints

### Main Application (Port 3000)
- `GET /api/subscriptions` - Get all subscriptions
- `GET /api/subscriptions/active` - Get active subscriptions
- `GET /api/subscriptions/{id}` - Get subscription by ID
- `PUT /api/subscriptions/{id}` - Update subscription
- `DELETE /api/subscriptions/{id}` - Delete subscription
- `POST /api/subscriptions` - **Delegates to microservice**

### Create Subscription Service (Port 3001)
- `POST /api/subscriptions` - Create new subscription
- `GET /api/subscriptions/health` - Health check

## Data Consistency

Both services share the same PostgreSQL database to ensure data consistency:
- **Create Service**: Handles all subscription creation
- **Main Service**: Handles all read, update, and delete operations
- **Shared Database**: Ensures both services see the same data

## Service Discovery

All services register with Eureka Server:
- **Eureka Dashboard**: http://localhost:8761
- Shows all registered services and their instances
- Provides health status and metadata

## Circuit Breaker

The main application uses Resilience4j circuit breaker when calling the create subscription service:
- **Fallback**: Returns 503 Service Unavailable if microservice is down
- **Configuration**: 50% failure threshold, 5-second wait duration

## Testing the Microservice

### Test Create Subscription via Main App
```bash
curl -X POST http://localhost:3000/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Service",
    "description": "Test Description",
    "amount": 29.99,
    "frequency": "MONTHLY"
  }'
```

### Test Create Subscription Directly
```bash
curl -X POST http://localhost:3001/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Direct Test",
    "description": "Direct Test Description",
    "amount": 19.99,
    "frequency": "YEARLY"
  }'
```

## Benefits Achieved

1. **Complete Separation of Concerns**: Create subscription logic is fully isolated in its own service
2. **Independent Deployment**: Can deploy create service separately from main application
3. **Fault Isolation**: Create service failures don't affect read/update/delete operations
4. **Scalability**: Can scale create service independently based on creation load
5. **Technology Flexibility**: Can use different technologies for each service
6. **Single Source of Truth**: All subscription creation goes through one service
7. **Clean Architecture**: Main application focuses only on read/update/delete operations 