# Deployment Guide

This guide covers production deployment of the Payment Gateway Service.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Deployment Options](#deployment-options)
- [Docker Deployment](#docker-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Database Setup](#database-setup)
- [Monitoring Setup](#monitoring-setup)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Infrastructure Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 2 cores | 4+ cores |
| RAM | 4 GB | 8+ GB |
| Storage | 20 GB | 100+ GB SSD |

### External Services

- **PostgreSQL 16** - Primary database
- **Apache Kafka** - Event streaming
- **Redis** - Caching and idempotency
- **Zipkin** - Distributed tracing (optional)
- **Prometheus/Grafana** - Monitoring (optional)

---

## Environment Configuration

### Required Environment Variables

```bash
# Database
DATASOURCE_URL=jdbc:postgresql://db-host:5432/payment_gateway
DATASOURCE_USERNAME=<secure-username>
DATASOURCE_PASSWORD=<secure-password>

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092

# Redis
REDIS_HOST=redis-host
REDIS_PORT=6379
REDIS_PASSWORD=<redis-password>

# JWT
JWT_SECRET=<jwt-secret-min-32-chars>
JWT_EXPIRATION_MS=86400000

# Encryption
ENCRYPTION_KEY=<32-char-encryption-key>
```

### Optional Environment Variables

```bash
# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Monitoring
ZIPKIN_BASE_URL=http://zipkin:9411

# AWS (for S3 report storage)
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=<access-key>
AWS_SECRET_ACCESS_KEY=<secret-key>
AWS_S3_BUCKET_NAME=payment-gateway-reports
```

---

## Deployment Options

### Option 1: Docker Compose (Recommended for Small Scale)

Best for: Small deployments, quick setup

### Option 2: Kubernetes (Recommended for Production)

Best for: Large scale, high availability, enterprise

---

## Docker Deployment

### 1. Prepare Environment

```bash
# Copy environment file
cp .env.example .env

# Edit with production values
nano .env
```

### 2. Build Image

```bash
docker build -t payment-gateway-service:v1.0.0 .
```

### 3. Deploy with Docker Compose

```bash
# Use production compose file
docker-compose -f docker-compose.prod.yml up -d
```

### 4. Verify Deployment

```bash
# Check health
curl http://localhost:8080/actuator/health

# Check logs
docker logs payment-gateway-service-app
```

---

## Kubernetes Deployment

### 1. Create Namespace

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: payment-gateway
```

```bash
kubectl apply -f namespace.yaml
```

### 2. Create Secrets

```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: payment-gateway-secrets
  namespace: payment-gateway
type: Opaque
stringData:
  DATASOURCE_PASSWORD: <password>
  JWT_SECRET: <jwt-secret>
  ENCRYPTION_KEY: <encryption-key>
```

```bash
kubectl apply -f secrets.yaml
```

### 3. Create ConfigMap

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-gateway-config
  namespace: payment-gateway
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DATASOURCE_URL: "jdbc:postgresql://postgres:5432/payment_gateway"
  KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
  REDIS_HOST: "redis"
```

### 4. Deploy Application

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-gateway
  namespace: payment-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment-gateway
  template:
    metadata:
      labels:
        app: payment-gateway
    spec:
      containers:
      - name: payment-gateway
        image: payment-gateway-service:v1.0.0
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: payment-gateway-config
        - secretRef:
            name: payment-gateway-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

```bash
kubectl apply -f deployment.yaml
```

### 5. Create Service

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: payment-gateway
  namespace: payment-gateway
spec:
  selector:
    app: payment-gateway
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 6. Create Ingress

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: payment-gateway-ingress
  namespace: payment-gateway
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - api.payment-gateway.com
    secretName: tls-secret
  rules:
  - host: api.payment-gateway.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: payment-gateway
            port:
              number: 80
```

---

## Database Setup

### PostgreSQL Configuration

```sql
-- Create database
CREATE DATABASE payment_gateway;

-- Create user
CREATE USER payment_user WITH PASSWORD 'secure_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE payment_gateway TO payment_user;
```

### Connection Pooling

Recommended settings for production:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1800000
```

### Backup Strategy

```bash
# Daily backup script
pg_dump -U payment_user -d payment_gateway -F c -f backup_$(date +%Y%m%d).dump

# Restore
pg_restore -U payment_user -d payment_gateway backup_20260320.dump
```

---

## Monitoring Setup

### Prometheus Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'payment-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['payment-gateway:8080']
```

### Grafana Dashboard

1. Import dashboard from `docker/grafana/dashboards/`
2. Configure Prometheus data source
3. Set up alerts for critical metrics

### Key Metrics to Monitor

| Metric | Alert Threshold |
|--------|----------------|
| HTTP 5xx errors | > 1% of requests |
| Response time p99 | > 500ms |
| Database connections | > 80% of pool |
| JVM memory | > 85% usage |
| Kafka consumer lag | > 1000 messages |

---

## Troubleshooting

### Application Won't Start

1. Check logs: `kubectl logs -n payment-gateway deployment/payment-gateway`
2. Verify environment variables
3. Check database connectivity
4. Verify Kafka connectivity

### High Memory Usage

1. Check JVM heap: `/actuator/metrics/jvm.memory.used`
2. Analyze heap dump
3. Review garbage collection logs

### Database Connection Issues

1. Check connection pool: `/actuator/metrics/hikaricp.connections`
2. Verify PostgreSQL is running
3. Check network connectivity
4. Review PostgreSQL logs

### Kafka Issues

1. Check consumer lag: Kafka UI
2. Verify topic exists
3. Check broker connectivity
4. Review consumer logs