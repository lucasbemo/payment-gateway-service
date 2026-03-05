-- V7__create_customers_table.sql
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    merchant_id VARCHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customers_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    CONSTRAINT uk_customers_merchant_token UNIQUE (merchant_id, token)
);

CREATE INDEX idx_customers_merchant_id ON customers(merchant_id);
CREATE INDEX idx_customers_email ON customers(email);
