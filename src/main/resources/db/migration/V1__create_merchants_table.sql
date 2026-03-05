-- V1__create_merchants_table.sql
CREATE TABLE merchants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    api_key_hash VARCHAR(512) NOT NULL,
    api_secret_hash VARCHAR(512) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    webhook_url VARCHAR(512),
    webhook_secret VARCHAR(512),
    configuration JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_merchants_email UNIQUE (email),
    CONSTRAINT uk_merchants_api_key UNIQUE (api_key_hash)
);

CREATE INDEX idx_merchants_status ON merchants(status);
CREATE INDEX idx_merchants_created_at ON merchants(created_at);
