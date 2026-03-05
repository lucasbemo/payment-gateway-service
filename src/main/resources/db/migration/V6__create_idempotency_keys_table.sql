-- V6__create_idempotency_keys_table.sql
CREATE TABLE idempotency_keys (
    id VARCHAR(36) PRIMARY KEY,
    key_hash VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    request_method VARCHAR(10),
    request_path VARCHAR(512),
    request_headers JSONB,
    request_body JSONB,
    response_code INTEGER,
    response_body JSONB,
    locked_until TIMESTAMP,
    locked_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotency_keys_hash UNIQUE (key_hash),
    CONSTRAINT fk_idempotency_keys_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

CREATE INDEX idx_idempotency_keys_merchant_id ON idempotency_keys(merchant_id);
CREATE INDEX idx_idempotency_keys_status ON idempotency_keys(status);
CREATE INDEX idx_idempotency_keys_locked_until ON idempotency_keys(locked_until);
