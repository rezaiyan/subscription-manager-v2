# Subscription Manager Monitoring

This monitoring application provides two different interfaces for monitoring the health of your microservices:

## 1. Web Dashboard (Default)

A web-based dashboard served via Ktor that provides real-time monitoring of all services.

### Features:
- Real-time health status monitoring
- Service response time tracking
- Auto-refresh every 30 seconds
- Activity logs
- Responsive design
- Beautiful gradient UI

### Usage:
```bash
# Start the web monitoring dashboard
./gradlew :monitoring-app:run
```

Then open your browser to `http://localhost:8082`

## 2. Desktop Application

A native desktop application built with Compose for Desktop that provides the same monitoring capabilities.

### Features:
- Native desktop application
- Same monitoring capabilities as web version
- Better performance for local monitoring
- No browser required

### Usage:
```bash
# Start the desktop application
./gradlew :monitoring-app:run --args="desktop"
```

Or use the convenience script:
```bash
chmod +x start-desktop.sh
./start-desktop.sh
```

## API Endpoints

The monitoring application provides the following REST endpoints:

- `GET /` - Web dashboard UI
- `GET /health` - Get health status of all services
- `GET /health/{serviceName}` - Get health status of a specific service

## Configuration

The monitoring application automatically detects and monitors the following services:

- Eureka Server (Discovery)
- Config Server
- API Gateway
- Main Server
- Create Subscription Service

## Building

```bash
# Build the entire project
./gradlew build

# Build only the monitoring app
./gradlew :monitoring-app:build
```

## Dependencies

- Kotlin 2.2.0
- Ktor 3.1.0
- Compose Multiplatform 1.8.2
- Jackson for JSON serialization

## Features

- 🚀 **Fast & Lightweight**: Built with Kotlin and Ktor
- 🔍 **Real-time Health Checks**: Monitors all services in real-time
- 📊 **Beautiful Dashboard**: Modern web interface with auto-refresh
- 🔌 **RESTful API**: JSON endpoints for programmatic access
- 🛡️ **CORS Support**: Cross-origin requests enabled
- 📱 **Responsive Design**: Works on desktop and mobile

## Services Monitored

- **Infrastructure**: Zookeeper, Kafka, PostgreSQL (Main & Create)
- **Service Discovery**: Eureka Server
- **Configuration**: Config Server
- **Business Services**: Create Subscription Service, Main Server
- **Gateway**: API Gateway
- **Frontend**: Website

## Quick Start

### Prerequisites

- Java 17 or later
- Gradle (included via wrapper)

### Running the App

1. **Using the start script (recommended):**
   ```bash
   ./start-monitoring.sh
   ```

2. **Using Gradle directly:**
   ```bash
   ./gradlew run
   ```

3. **Build and run JAR:**
   ```bash
   ./gradlew build
   java -jar build/libs/monitoring-app-1.0-SNAPSHOT.jar
   ```

### Accessing the App

- **Dashboard**: http://localhost:8083/dashboard
- **Health API**: http://localhost:8083/health
- **API Documentation**: http://localhost:8083/

## API Endpoints

### Get All Services Health
```http
GET /health
```

**Response:**
```json
{
  "timestamp": 1703123456,
  "overall": {
    "total": 10,
    "up": 9,
    "down": 1,
    "status": "warning"
  },
  "services": [
    {
      "service": {
        "name": "PostgreSQL Main",
        "url": "localhost:5432",
        "healthEndpoint": null,
        "category": "infrastructure"
      },
      "health": {
        "status": "up",
        "responseTime": 15,
        "details": "Port accessible",
        "statusCode": null
      }
    }
  ]
}
```

### Get Single Service Health
```http
GET /health/{service-name}
```

**Example:**
```http
GET /health/postgresql-main
```

## Health Check Logic

### HTTP Services
- Checks `/actuator/health` endpoints
- Returns status based on HTTP response codes
- Special handling for API Gateway (404 is considered normal)

### Infrastructure Services
- **PostgreSQL**: TCP socket connection test
- **Kafka/Zookeeper**: TCP socket connection test
- **Others**: HTTP HEAD request to port

### Status Types
- **up**: Service is healthy and responding
- **warning**: Service is responding but with non-200 status
- **down**: Service is not accessible or has errors

## Development

### Project Structure
```
monitoring-app/
├── src/main/kotlin/
│   └── com/alirezaiyan/subscriptionmanager/monitoring/
│       ├── Application.kt          # Main Ktor application
│       ├── HealthChecker.kt        # Health check logic
│       ├── Models.kt               # Data classes
│       └── ServiceConfig.kt        # Service configuration
├── src/main/resources/
│   ├── logback.xml                 # Logging configuration
│   └── static/                     # Static resources
├── build.gradle.kts                # Build configuration
├── start-monitoring.sh             # Start script
└── README.md                       # This file
```

### Adding New Services

1. Add service configuration in `ServiceConfig.kt`:
   ```kotlin
   Service(
       name = "New Service",
       url = "http://localhost:8084",
       healthEndpoint = "http://localhost:8084/actuator/health",
       category = "business-service"
   )
   ```

2. The health checker will automatically detect and monitor the new service.

### Custom Health Check Logic

Modify `HealthChecker.kt` to add custom health check logic for specific services:

```kotlin
private fun checkCustomService(service: Service, startTime: Long): HealthStatus {
    // Custom health check logic
    return HealthStatus(
        status = "up",
        responseTime = System.currentTimeMillis() - startTime,
        details = "Custom health check passed"
    )
}
```

## Configuration

### Port Configuration
Change the port in `Application.kt`:
```kotlin
embeddedServer(Netty, port = 8083, host = "0.0.0.0", module = Application::module)
```

### Service Configuration
Modify service endpoints in `ServiceConfig.kt` to match your environment.

### Logging
Configure logging levels in `src/main/resources/logback.xml`.

## Troubleshooting

### Common Issues

1. **Port already in use**
   - Change the port in `Application.kt`
   - Or stop the existing service using port 8083

2. **Services showing as down**
   - Check if services are actually running
   - Verify endpoints in `ServiceConfig.kt`
   - Check network connectivity

3. **Build errors**
   - Ensure Java 17+ is installed
   - Run `./gradlew clean build`

### Logs
Check the console output for detailed logs and error messages.

## Performance

- **Response Time**: Health checks complete in ~1-5 seconds
- **Memory Usage**: ~50-100MB
- **CPU Usage**: Minimal (only during health checks)
- **Auto-refresh**: Dashboard refreshes every 30 seconds

## Security

- CORS is enabled for all origins (development)
- No authentication required (monitoring only)
- Consider adding authentication for production use

## License

This project is part of the Subscription Manager system. 