# Monitoring and Observability Setup

## Components

### 1. Prometheus (Metrics Collection)
- **Port**: 9090
- **Purpose**: Collect metrics from all microservices
- **Configuration**: `prometheus.yml`

### 2. Grafana (Visualization)
- **Port**: 3000
- **Purpose**: Dashboard for metrics visualization
- **Dashboards**: Pre-configured for Spring Boot applications

### 3. Jaeger (Distributed Tracing)
- **Port**: 16686
- **Purpose**: Trace requests across microservices
- **Configuration**: Automatic instrumentation

## Setup

### 1. Add Micrometer to Services
Add to each microservice's `build.gradle.kts`:
```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-registry-prometheus")
```

### 2. Configure Prometheus
Create `monitoring/prometheus.yml`:
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'subscription-manager'
    static_configs:
      - targets: ['localhost:3000']
    metrics_path: '/actuator/prometheus'
    
  - job_name: 'create-subscription-service'
    static_configs:
      - targets: ['localhost:3001']
    metrics_path: '/actuator/prometheus'
    
  - job_name: 'api-gateway'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### 3. Add to Docker Compose
```yaml
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      
  jaeger:
    image: jaegertracing/all-in-one
    ports:
      - "16686:16686"
      - "14268:14268"
```

## Metrics Available

### Application Metrics
- HTTP request rates and latencies
- Database connection pool metrics
- JVM memory and GC metrics
- Custom business metrics

### Business Metrics
- Subscription creation rate
- Active subscriptions count
- Revenue calculations
- Error rates by service

## Dashboards

### Spring Boot Dashboard
- Application health
- Request rates and latencies
- Error rates
- Database metrics

### Microservice Dashboard
- Service-to-service communication
- Circuit breaker status
- Service discovery health
- API gateway metrics 