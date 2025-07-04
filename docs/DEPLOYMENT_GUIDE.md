# Subscription Manager Microservice Deployment Guide

This guide provides comprehensive instructions for deploying the Subscription Manager microservice architecture on a remote server.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Manual Deployment](#manual-deployment)
4. [Docker Deployment](#docker-deployment)
5. [Production Deployment](#production-deployment)
6. [Monitoring and Maintenance](#monitoring-and-maintenance)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements
- **OS**: Ubuntu 20.04+ / CentOS 8+ / RHEL 8+
- **CPU**: 2+ cores
- **RAM**: 4GB+ (8GB recommended)
- **Storage**: 20GB+ free space
- **Network**: Ports 3000, 3001, 5432, 8761, 8080 available

### Software Requirements
- **Java**: OpenJDK 17 or higher
- **PostgreSQL**: 13 or higher
- **Docker**: 20.10+ (for Docker deployment)
- **Git**: Latest version

## Quick Start

### Option 1: Automated Deployment Script

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd subscription-manager
   ```

2. **Make the deployment script executable**:
   ```bash
   chmod +x deploy.sh
   ```

3. **Run the deployment**:
   ```bash
   ./deploy.sh deploy
   ```

4. **Check status**:
   ```bash
   ./deploy.sh status
   ```

### Option 2: Docker Compose

1. **Start all services**:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

2. **Check logs**:
   ```bash
   docker-compose -f docker-compose.prod.yml logs -f
   ```

## Manual Deployment

### Step 1: Install Prerequisites

#### Ubuntu/Debian
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java
sudo apt install openjdk-17-jdk -y

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib -y

# Install additional tools
sudo apt install curl wget git -y
```

#### CentOS/RHEL
```bash
# Update system
sudo yum update -y

# Install Java
sudo yum install java-17-openjdk-devel -y

# Install PostgreSQL
sudo yum install postgresql postgresql-server postgresql-contrib -y

# Initialize PostgreSQL
sudo postgresql-setup initdb
sudo systemctl enable postgresql
sudo systemctl start postgresql

# Install additional tools
sudo yum install curl wget git -y
```

### Step 2: Configure PostgreSQL

1. **Create database user**:
   ```bash
   sudo -u postgres createuser --interactive subscription_user
   sudo -u postgres createdb subscription_main_db
   sudo -u postgres createdb subscription_create_db
   ```

2. **Set password**:
   ```bash
   sudo -u postgres psql
   ALTER USER subscription_user WITH PASSWORD 'subscription_password';
   GRANT ALL PRIVILEGES ON DATABASE subscription_main_db TO subscription_user;
   GRANT ALL PRIVILEGES ON DATABASE subscription_create_db TO subscription_user;
   \q
   ```

3. **Configure PostgreSQL for remote connections** (if needed):
   ```bash
   sudo nano /etc/postgresql/*/main/postgresql.conf
   # Change: listen_addresses = '*'
   
   sudo nano /etc/postgresql/*/main/pg_hba.conf
   # Add: host all all 0.0.0.0/0 md5
   
   sudo systemctl restart postgresql
   ```

### Step 3: Deploy Application

1. **Clone and setup**:
   ```bash
   git clone <repository-url>
   cd subscription-manager
   chmod +x deploy.sh
   ```

2. **Configure environment** (edit deploy.sh if needed):
   ```bash
   # Update database configuration in deploy.sh
   DB_USER="subscription_user"
   DB_PASSWORD="subscription_password"
   ```

3. **Run deployment**:
   ```bash
   ./deploy.sh deploy
   ```

## Docker Deployment

### Prerequisites
- Docker and Docker Compose installed
- Ports 3000, 3001, 5432, 8761, 8080 available

### Deployment Steps

1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd subscription-manager
   ```

2. **Build and start services**:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d --build
   ```

3. **Check status**:
   ```bash
   docker-compose -f docker-compose.prod.yml ps
   ```

4. **View logs**:
   ```bash
   docker-compose -f docker-compose.prod.yml logs -f
   ```

### Docker Commands

```bash
# Start services
docker-compose -f docker-compose.prod.yml up -d

# Stop services
docker-compose -f docker-compose.prod.yml down

# Restart services
docker-compose -f docker-compose.prod.yml restart

# View logs
docker-compose -f docker-compose.prod.yml logs -f [service-name]

# Scale services
docker-compose -f docker-compose.prod.yml up -d --scale create-subscription-service=2
```

## Production Deployment

### Systemd Service Setup

1. **Create service user**:
   ```bash
   sudo useradd -r -s /bin/false subscription
   sudo mkdir -p /opt/subscription-manager
   sudo chown subscription:subscription /opt/subscription-manager
   ```

2. **Copy application**:
   ```bash
   sudo cp -r . /opt/subscription-manager/
   sudo chown -R subscription:subscription /opt/subscription-manager
   ```

3. **Install systemd service**:
   ```bash
   sudo cp subscription-manager.service /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable subscription-manager
   ```

4. **Start service**:
   ```bash
   sudo systemctl start subscription-manager
   sudo systemctl status subscription-manager
   ```

### Nginx Reverse Proxy (Optional)

1. **Install Nginx**:
   ```bash
   sudo apt install nginx -y
   ```

2. **Create Nginx configuration**:
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           proxy_pass http://localhost:3000;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
       
       location /api/subscriptions/ {
           proxy_pass http://localhost:3001/api/subscriptions/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
       
       location /eureka/ {
           proxy_pass http://localhost:8761/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```

3. **Enable and restart Nginx**:
   ```bash
   sudo systemctl enable nginx
   sudo systemctl restart nginx
   ```

## Monitoring and Maintenance

### Health Checks

```bash
# Check service status
./deploy.sh status

# Perform health checks
./deploy.sh health

# Test functionality
./deploy.sh test
```

### Log Management

```bash
# View available logs
./deploy.sh logs

# Monitor specific service logs
tail -f logs/eureka.log
tail -f logs/create-service.log
tail -f logs/main-app.log

# Docker logs
docker-compose -f docker-compose.prod.yml logs -f [service-name]
```

### Backup and Recovery

1. **Database backup**:
   ```bash
   pg_dump -h localhost -U subscription_user subscription_main_db > backup_main_$(date +%Y%m%d).sql
   pg_dump -h localhost -U subscription_user subscription_create_db > backup_create_$(date +%Y%m%d).sql
   ```

2. **Application backup**:
   ```bash
   tar -czf subscription-manager-backup-$(date +%Y%m%d).tar.gz /opt/subscription-manager/
   ```

### Performance Monitoring

1. **JVM monitoring**:
   ```bash
   # Check JVM memory usage
   jstat -gc <pid>
   
   # Check thread dump
   jstack <pid>
   ```

2. **Database monitoring**:
   ```bash
   # Check PostgreSQL connections
   psql -U subscription_user -d subscription_main_db -c "SELECT count(*) FROM pg_stat_activity;"
   
   # Check slow queries
   psql -U subscription_user -d subscription_main_db -c "SELECT * FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"
   ```

## Troubleshooting

### Common Issues

#### 1. Service Won't Start
```bash
# Check prerequisites
./deploy.sh health

# Check logs
tail -f logs/*.log

# Check port availability
netstat -tulpn | grep :3000
netstat -tulpn | grep :3001
netstat -tulpn | grep :8761
```

#### 2. Database Connection Issues
```bash
# Test database connection
psql -h localhost -U subscription_user -d subscription_main_db

# Check PostgreSQL status
sudo systemctl status postgresql

# Check PostgreSQL logs
sudo tail -f /var/log/postgresql/postgresql-*.log
```

#### 3. Service Discovery Issues
```bash
# Check Eureka registration
curl -s http://localhost:8761/eureka/apps

# Check service health
curl -s http://localhost:3001/api/subscriptions/health
curl -s http://localhost:3000/api/subscriptions
```

#### 4. Memory Issues
```bash
# Check memory usage
free -h
ps aux | grep java

# Increase JVM heap size (edit application.properties)
# Add: JAVA_OPTS="-Xmx2g -Xms1g"
```

### Log Analysis

```bash
# Search for errors
grep -i error logs/*.log

# Search for specific service
grep -i "create-subscription-service" logs/*.log

# Monitor real-time logs
tail -f logs/*.log | grep -i error
```

### Recovery Procedures

#### 1. Service Recovery
```bash
# Stop all services
./deploy.sh stop

# Clean up processes
pkill -f "gradlew.*bootRun"

# Restart services
./deploy.sh deploy
```

#### 2. Database Recovery
```bash
# Restore from backup
psql -U subscription_user -d subscription_main_db < backup_main_20231201.sql
psql -U subscription_user -d subscription_create_db < backup_create_20231201.sql
```

#### 3. Complete Reset
```bash
# Stop services
./deploy.sh stop

# Drop and recreate databases
dropdb -U subscription_user subscription_main_db
dropdb -U subscription_user subscription_create_db
createdb -U subscription_user subscription_main_db
createdb -U subscription_user subscription_create_db

# Redeploy
./deploy.sh deploy
```

## Security Considerations

1. **Firewall Configuration**:
   ```bash
   # Allow only necessary ports
   sudo ufw allow 22/tcp    # SSH
   sudo ufw allow 80/tcp    # HTTP (if using Nginx)
   sudo ufw allow 443/tcp   # HTTPS (if using SSL)
   sudo ufw enable
   ```

2. **SSL/TLS Configuration**:
   - Use Let's Encrypt for free SSL certificates
   - Configure Nginx with SSL termination
   - Enable HTTPS for all API endpoints

3. **Database Security**:
   - Use strong passwords
   - Limit database access to application servers only
   - Regular security updates

4. **Application Security**:
   - Keep Java and dependencies updated
   - Use environment variables for sensitive data
   - Implement proper authentication and authorization

## Support

For additional support:
- Check the logs: `./deploy.sh logs`
- Review this deployment guide
- Check the project documentation
- Open an issue in the project repository

---

**Note**: This deployment guide assumes a Linux environment. For Windows or macOS deployments, some commands may need to be adjusted accordingly. 