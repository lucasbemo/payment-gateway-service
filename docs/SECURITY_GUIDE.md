# Security Guide

This document outlines security best practices, PCI DSS compliance considerations, and incident response procedures for the Payment Gateway Service.

---

## Table of Contents

- [Security Overview](#security-overview)
- [Authentication & Authorization](#authentication--authorization)
- [Data Protection](#data-protection)
- [PCI DSS Compliance](#pci-dss-compliance)
- [Secrets Management](#secrets-management)
- [Security Checklist](#security-checklist)
- [Incident Response](#incident-response)

---

## Security Overview

### Architecture Security

```
┌─────────────────────────────────────────────────────────────┐
│                      SECURITY LAYERS                         │
├─────────────────────────────────────────────────────────────┤
│  1. API Gateway / Load Balancer (TLS termination)           │
│  2. Authentication (API Key / JWT)                          │
│  3. Authorization (Role-based access)                       │
│  4. Input Validation (Request validation)                   │
│  5. Data Encryption (AES-256 at rest)                       │
│  6. Network Security (VPC, Security Groups)                 │
└─────────────────────────────────────────────────────────────┘
```

### Security Features Implemented

| Feature | Implementation | Status |
|---------|---------------|--------|
| Authentication | API Key + JWT | ✅ |
| Authorization | Spring Security | ✅ |
| Input Validation | Jakarta Validation | ✅ |
| SQL Injection Prevention | Parameterized queries | ✅ |
| XSS Prevention | Input sanitization | ✅ |
| CSRF Protection | Spring Security | ✅ |
| Rate Limiting | Resilience4j | ✅ |
| Encryption at Rest | AES-256-GCM | ✅ |
| Card Tokenization | Custom tokenization | ✅ |
| Audit Logging | Structured logging | ✅ |

---

## Authentication & Authorization

### API Key Authentication

Used for merchant-to-gateway communication.

```http
GET /api/v1/payments HTTP/1.1
Host: api.payment-gateway.com
X-API-Key: pk_live_xxxxxxxxxxxx
```

**Best Practices:**
- Rotate API keys every 90 days
- Use different keys for different environments
- Store keys securely (never in code)
- Monitor key usage for anomalies

### JWT Authentication

Used for user/admin authentication.

```http
GET /api/v1/admin/merchants HTTP/1.1
Host: api.payment-gateway.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**JWT Configuration:**
```yaml
security:
  jwt:
    secret: ${JWT_SECRET}  # Min 32 characters
    expiration-ms: 3600000  # 1 hour
```

### Role-Based Access Control

| Role | Permissions |
|------|-------------|
| `MERCHANT` | Own resources only |
| `MERCHANT_ADMIN` | Merchant management |
| `SYSTEM_ADMIN` | Full access |

---

## Data Protection

### Sensitive Data Classification

| Data Type | Classification | Protection |
|-----------|---------------|------------|
| Card Numbers (PAN) | Critical | Tokenization + Encryption |
| CVV | Critical | Never stored |
| API Keys | High | Encrypted at rest |
| Customer PII | Medium | Encryption optional |
| Transaction Data | Medium | Standard protection |

### Card Tokenization

Card numbers are never stored in plain text. They are replaced with tokens:

```
4111111111111111 → tok_visa_abc123xyz
```

**Tokenization Flow:**
1. Card number received via API
2. Immediately tokenize via tokenization service
3. Store only the token
4. Use token for subsequent operations

### Encryption at Rest

AES-256-GCM encryption for sensitive fields:

```java
@Component
public class EncryptionService {
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    public String encrypt(String plaintext) {
        // AES-256-GCM encryption
    }
    
    public String decrypt(String ciphertext) {
        // AES-256-GCM decryption
    }
}
```

---

## PCI DSS Compliance

### PCI DSS Requirements Mapping

| Requirement | Implementation | Status |
|-------------|---------------|--------|
| **1. Firewall** | Network security groups | ✅ |
| **2. Default Passwords** | No default passwords | ✅ |
| **3. Stored Data** | Tokenization + Encryption | ✅ |
| **4. Encrypted Transmission** | TLS 1.2+ | ✅ |
| **5. Anti-Malware** | Regular scanning | ⚠️ Customer responsibility |
| **6. Secure Systems** | Patch management | ⚠️ Customer responsibility |
| **7. Access Control** | RBAC + MFA | ✅ |
| **8. Unique IDs** | Unique user IDs | ✅ |
| **9. Physical Access** | Data center security | ⚠️ Cloud provider |
| **10. Network Monitoring** | Logging + Alerting | ✅ |
| **11. Security Testing** | Vulnerability scans | ✅ |
| **12. Information Policy** | Security policy | ⚠️ Customer responsibility |

### Cardholder Data Storage

**What we DO NOT store:**
- Full card numbers (PAN)
- CVV/CVC codes
- PINs
- Track data

**What we DO store (encrypted):**
- Last 4 digits of card
- Card expiry (MM/YY)
- Cardholder name
- Tokens

---

## Secrets Management

### Environment Variables

All secrets must be provided via environment variables:

```bash
# Required secrets
DATASOURCE_PASSWORD=<db-password>
JWT_SECRET=<jwt-secret>
ENCRYPTION_KEY=<encryption-key>
```

### Secret Rotation Schedule

| Secret | Rotation Frequency |
|--------|-------------------|
| API Keys | Every 90 days |
| JWT Secret | Every 30 days |
| Database Password | Every 30 days |
| Encryption Key | Every 90 days (requires data re-encryption) |

### GitHub Secrets (for CI/CD)

Configure in GitHub repository settings:

- `ALIBABA_API_KEY` - AI Code Review API key
- `ALIBABA_BASE_URL` - API base URL
- `GITHUB_TOKEN` - Automatic

---

## Security Checklist

### Before Deployment

- [ ] All default passwords changed
- [ ] TLS 1.2+ enabled
- [ ] API keys rotated
- [ ] JWT secret is strong (32+ chars)
- [ ] Encryption key is strong (32 chars)
- [ ] Database access restricted
- [ ] Rate limiting enabled
- [ ] Logging configured (without sensitive data)
- [ ] Security scan passed
- [ ] Dependencies updated

### Regular Maintenance

- [ ] Weekly: Review security logs
- [ ] Monthly: Rotate secrets
- [ ] Quarterly: Vulnerability scan
- [ ] Annually: Penetration test

---

## Incident Response

### Severity Levels

| Level | Description | Response Time |
|-------|-------------|---------------|
| **P1 - Critical** | Data breach, system compromise | 15 minutes |
| **P2 - High** | Security vulnerability exploited | 1 hour |
| **P3 - Medium** | Suspicious activity detected | 4 hours |
| **P4 - Low** | Security policy violation | 24 hours |

### Incident Response Steps

1. **Identify** - Detect and confirm the incident
2. **Contain** - Isolate affected systems
3. **Eradicate** - Remove the threat
4. **Recover** - Restore normal operations
5. **Review** - Post-incident analysis

### Emergency Contacts

| Role | Contact |
|------|---------|
| Security Team | security@payment-gateway.com |
| On-Call Engineer | +1-xxx-xxx-xxxx |
| Legal/Compliance | legal@payment-gateway.com |

### Incident Reporting

Report security incidents to: `security@payment-gateway.com`

Include:
- Description of the incident
- Time discovered
- Systems affected
- Steps already taken
- Contact information