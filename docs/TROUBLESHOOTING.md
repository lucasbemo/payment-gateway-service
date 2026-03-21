# Troubleshooting Guide

This guide helps you diagnose and resolve common issues with the Payment Gateway Service.

---

## Table of Contents

- [Application Won't Start](#application-wont-start)
- [Database Issues](#database-issues)
- [Kafka Issues](#kafka-issues)
- [Redis Issues](#redis-issues)
- [Payment Processing Issues](#payment-processing-issues)
- [Performance Issues](#performance-issues)
- [Security Issues](#security-issues)
- [Docker Issues](#docker-issues)
- [FAQ](#faq)

---

## Application Won't Start

### Symptom: Port Already in Use

```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
```bash
# Find process using port
lsof -i :8080
# or
netstat -tulpn | grep 8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### Symptom: Database Connection Failed

```
Unable to open JDBC Connection for DDL execution
```

**Solution:**
1. Check PostgreSQL is running:
   ```bash
   docker ps | grep postgres
   ```

2. Check connection details in `application.yml`

3. Verify credentials:
   ```bash
   docker exec -it payment-gateway-service-postgres psql -U admin -d payment_gateway
   ```

### Symptom: Kafka Connection Failed

```
Connection to node -1 could not be established
```

**Solution:**
1. Check Kafka is running:
   ```bash
   docker ps | grep kafka
   ```

2. Check Kafka logs:
   ```bash
   docker logs payment-gateway-service-kafka
   ```

3. Verify bootstrap servers in `application.yml`:
   ```yaml
   spring:
     kafka:
       bootstrap-servers: localhost:19092
   ```

---

## Database Issues

### Symptom: Migration Failed

```
FlywayException: Validate failed: migrations have changed
```

**Solution:**
```bash
# Check migration status
./mvnw flyway:info

# Repair if needed
./mvnw flyway:repair

# Or clean and rebuild
docker-compose down -v
docker-compose up -d postgres
```

### Symptom: Connection Pool Exhausted

```
HikariPool - Connection is not available
```

**Solution:**
1. Increase pool size:
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 30
   ```

2. Check for connection leaks in code

3. Monitor active connections:
   ```bash
   docker exec -it payment-gateway-service-postgres \
     psql -U admin -d payment_gateway -c "SELECT count(*) FROM pg_stat_activity;"
   ```

### Symptom: Slow Queries

**Solution:**
1. Enable slow query logging:
   ```yaml
   spring:
     jpa:
       show-sql: true
       properties:
         hibernate:
           format_sql: true
   ```

2. Check indexes on frequently queried columns

3. Use `EXPLAIN ANALYZE` for query analysis

---

## Kafka Issues

### Symptom: Consumer Lag

**Diagnosis:**
```bash
# Check consumer groups
docker exec payment-gateway-service-kafka \
  kafka-consumer-groups --bootstrap-server localhost:29092 --list

# Check lag
docker exec payment-gateway-service-kafka \
  kafka-consumer-groups --bootstrap-server localhost:29092 \
  --describe --group payment-gateway-group
```

**Solution:**
1. Increase consumer instances
2. Optimize message processing
3. Check for errors in consumer logs

### Symptom: Topic Not Found

```
Unknown topic or partition during production
```

**Solution:**
1. Check topic exists:
   ```bash
   docker exec payment-gateway-service-kafka \
     kafka-topics --list --bootstrap-server localhost:29092
   ```

2. Create topic if needed:
   ```bash
   docker exec payment-gateway-service-kafka \
     kafka-topics --create --topic payment-events \
     --bootstrap-server localhost:29092 --partitions 3 --replication-factor 1
   ```

---

## Redis Issues

### Symptom: Redis Connection Refused

```
Unable to connect to Redis
```

**Solution:**
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
docker exec payment-gateway-service-redis redis-cli ping

# Check Redis logs
docker logs payment-gateway-service-redis
```

### Symptom: Redis Out of Memory

```
OOM command not allowed when used memory > 'maxmemory'
```

**Solution:**
1. Increase Redis memory:
   ```yaml
   # docker-compose.yml
   redis:
     command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
   ```

2. Clear unused keys:
   ```bash
   docker exec payment-gateway-service-redis redis-cli FLUSHDB
   ```

---

## Payment Processing Issues

### Symptom: Payment Declined

**Diagnosis:**
1. Check payment status in response
2. Review error code and message
3. Check gateway logs

**Common Causes:**
| Error Code | Cause | Solution |
|------------|-------|----------|
| `INSUFFICIENT_FUNDS` | Card has no funds | Use different card |
| `CARD_DECLINED` | Bank declined | Contact bank |
| `EXPIRED_CARD` | Card expired | Use valid card |
| `INVALID_CARD` | Invalid card number | Check card details |

### Symptom: Duplicate Payment

**Diagnosis:**
1. Check if idempotency key was used
2. Review payment logs

**Solution:**
- Use unique `X-Idempotency-Key` header for each payment
- Implement idempotency key caching

---

## Performance Issues

### Symptom: High CPU Usage

**Diagnosis:**
```bash
# Check CPU usage
top -p $(pgrep -f payment-gateway)

# Check thread dump
jstack <PID> > thread_dump.txt
```

**Solution:**
1. Review thread dump for hot spots
2. Check for infinite loops
3. Optimize heavy computations

### Symptom: High Memory Usage

**Diagnosis:**
```bash
# Check memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Generate heap dump
jmap -dump:format=b,file=heap.hprof <PID>
```

**Solution:**
1. Analyze heap dump with VisualVM or MAT
2. Look for memory leaks
3. Increase heap size if needed:
   ```bash
   java -Xmx2g -jar app.jar
   ```

### Symptom: Slow Response Times

**Diagnosis:**
```bash
# Check response time metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

**Solution:**
1. Add database indexes
2. Enable caching
3. Optimize queries
4. Scale horizontally

---

## Security Issues

### Symptom: Authentication Failed

```
401 Unauthorized
```

**Solution:**
1. Verify API key is correct
2. Check JWT token is valid and not expired
3. Verify Authorization header format:
   ```http
   Authorization: Bearer <token>
   ```

### Symptom: CSRF Token Missing

```
403 Forbidden - Invalid CSRF token
```

**Solution:**
- CSRF protection is disabled for API endpoints
- If enabled, include CSRF token in requests

---

## Docker Issues

### Symptom: Container Won't Start

```bash
# Check container logs
docker logs payment-gateway-service-app

# Check container status
docker ps -a | grep payment-gateway

# Check resource usage
docker stats
```

### Symptom: Volume Permission Denied

```
Permission denied on volume mount
```

**Solution:**
```bash
# Fix permissions
sudo chown -R $USER:$USER ./docker/volumes
```

### Symptom: Network Issues

```
Network payment-network not found
```

**Solution:**
```bash
# Recreate network
docker network create payment-network

# Or restart all services
docker-compose down
docker-compose up -d
```

---

## FAQ

### Q: How do I reset the database?

```bash
docker-compose down -v
docker-compose up -d postgres
```

### Q: How do I view API documentation?

```
http://localhost:8080/swagger-ui.html
```

### Q: How do I check application health?

```bash
curl http://localhost:8080/actuator/health
```

### Q: How do I access Kafka UI?

```
http://localhost:8082
```

### Q: How do I access pgAdmin?

```
http://localhost:8083
Email: admin@admin.com
Password: admin
```

### Q: How do I run a specific test?

```bash
./mvnw test -Dtest=PaymentServiceTest
```

### Q: How do I enable debug logging?

```yaml
logging:
  level:
    com.payment.gateway: DEBUG
```

---

## Getting Help

1. Check this troubleshooting guide
2. Review application logs
3. Check GitHub issues
4. Contact: support@payment-gateway.com