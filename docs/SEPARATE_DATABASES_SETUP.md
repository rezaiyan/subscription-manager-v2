# Separate Databases Setup

## 🗄️ Database Architecture

We've implemented **Database Per Service** pattern for true microservice isolation:

### Database Layout
```
┌─────────────────────────────────────────────────────────┐
│                    PostgreSQL Instances                 │
├─────────────────────────────────────────────────────────┤
│  Main Database (Port 5432)    │  Create DB (Port 5433)  │
│  subscription_main_db         │  subscription_create_db │
│                               │                         │
│  - Read operations            │  - Create operations    │
│  - Update operations          │  - Event publishing     │
│  - Delete operations          │  - Audit logging        │
│  - Business logic             │  - Validation           │
└─────────────────────────────────────────────────────────┘
```

## 🔧 Database Configuration

### Main Application Database
- **Port**: 5432
- **Database**: `subscription_main_db`
- **Purpose**: Read, update, delete operations
- **Connection**: `jdbc:postgresql://localhost:5432/subscription_main_db`

### Create Service Database
- **Port**: 5433
- **Database**: `subscription_create_db`
- **Purpose**: Subscription creation and event publishing
- **Connection**: `jdbc:postgresql://localhost:5433/subscription_create_db`

## 📊 Data Consistency Strategy

### Event-Driven Synchronization
Since each service owns its database, we use **event-driven synchronization**:

1. **Create Service** saves subscription to its database
2. **Create Service** publishes `SubscriptionCreatedEvent`
3. **Main Service** listens for events and syncs to its database
4. **Eventual Consistency** is achieved asynchronously

### Event Flow
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Create Service  │───▶│ Event Publisher │───▶│ Main Service    │
│ (Port 3001)     │    │ (Spring Events) │    │ (Port 3000)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Create Database │    │ Event Bus       │    │ Main Database   │
│ (Port 5433)     │    │ (In-Memory)     │    │ (Port 5432)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Setup Instructions

### 1. Database Initialization

#### Option A: Using Docker Compose (Recommended)
```bash
# Start databases
docker-compose up postgres-main postgres-create -d

# Initialize databases (optional - JPA will create tables)
docker exec -it <postgres-main-container> psql -U ali.rezaiyan -d subscription_main_db -f /path/to/init-main-db.sql
docker exec -it <postgres-create-container> psql -U ali.rezaiyan -d subscription_create_db -f /path/to/init-create-db.sql
```

#### Option B: Manual Setup
```bash
# Start PostgreSQL instances
# Main database on port 5432
# Create database on port 5433

# Run initialization scripts
psql -U ali.rezaiyan -d subscription_main_db -f database-setup/init-main-db.sql
psql -U ali.rezaiyan -d subscription_create_db -f database-setup/init-create-db.sql
```

### 2. Application Configuration

#### Main Application (`server/src/main/resources/application.properties`)
```properties
# Main database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/subscription_main_db
spring.datasource.username=ali.rezaiyan
spring.datasource.password=
```

#### Create Service (`create-subscription-service/src/main/resources/application.properties`)
```properties
# Create service database connection
spring.datasource.url=jdbc:postgresql://localhost:5433/subscription_create_db
spring.datasource.username=ali.rezaiyan
spring.datasource.password=
```

## 🧪 Testing Separate Databases

### 1. Test Database Isolation
```bash
# Create subscription via API Gateway
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Service", "amount": 29.99, "frequency": "MONTHLY"}'

# Check Create Service Database
psql -U ali.rezaiyan -d subscription_create_db -c "SELECT * FROM subscription;"

# Check Main Service Database
psql -U ali.rezaiyan -d subscription_main_db -c "SELECT * FROM subscription;"
```

### 2. Test Event Synchronization
```bash
# Create subscription directly via Create Service
curl -X POST http://localhost:3001/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"name": "Direct Test", "amount": 19.99, "frequency": "YEARLY"}'

# Verify data appears in both databases
psql -U ali.rezaiyan -d subscription_create_db -c "SELECT COUNT(*) FROM subscription;"
psql -U ali.rezaiyan -d subscription_main_db -c "SELECT COUNT(*) FROM subscription;"
```

## 🔍 Monitoring Database Health

### Health Checks
- **Main Database**: Check via main application health endpoint
- **Create Database**: Check via create service health endpoint

### Database Metrics
```bash
# Check database connections
psql -U ali.rezaiyan -d subscription_main_db -c "SELECT count(*) FROM pg_stat_activity;"
psql -U ali.rezaiyan -d subscription_create_db -c "SELECT count(*) FROM pg_stat_activity;"

# Check table sizes
psql -U ali.rezaiyan -d subscription_main_db -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) FROM pg_tables WHERE schemaname = 'public';"
psql -U ali.rezaiyan -d subscription_create_db -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) FROM pg_tables WHERE schemaname = 'public';"
```

## ⚠️ Important Considerations

### 1. **Eventual Consistency**
- Data synchronization is asynchronous
- Brief periods of inconsistency are possible
- Consider this in your application logic

### 2. **Event Reliability**
- Current implementation uses in-memory events
- For production, consider using message brokers (RabbitMQ/Kafka)
- Implement event replay mechanisms

### 3. **Database Backup**
- Each database needs separate backup strategies
- Consider point-in-time recovery for both databases
- Test restore procedures regularly

### 4. **Performance**
- Separate databases allow independent optimization
- Each service can have its own indexes and tuning
- Monitor performance separately

## 🔄 Production Recommendations

### 1. **Message Broker Integration**
```yaml
# Add to docker-compose.yml
rabbitmq:
  image: rabbitmq:3-management
  ports:
    - "5672:5672"
    - "15672:15672"
```

### 2. **Database Clustering**
- Consider read replicas for read-heavy operations
- Implement database sharding for large datasets
- Use connection pooling for better performance

### 3. **Monitoring & Alerting**
- Set up database monitoring (pgAdmin, Grafana)
- Implement alerts for database failures
- Monitor event synchronization lag

## ✅ Benefits of Separate Databases

1. **✅ True Service Isolation**: Each service owns its data
2. **✅ Independent Scaling**: Can scale databases separately
3. **✅ Technology Flexibility**: Can use different database types
4. **✅ Fault Isolation**: Database failures don't affect other services
5. **✅ Independent Deployment**: Can deploy database changes separately
6. **✅ Security**: Each service has its own database credentials
7. **✅ Performance**: Can optimize each database independently 