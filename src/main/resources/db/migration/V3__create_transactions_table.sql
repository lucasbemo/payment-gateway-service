-- V3__create_transactions_table.sql
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider VARCHAR(100),
    provider_transaction_id VARCHAR(255),
    provider_response JSONB,
    error_message TEXT,
    error_code VARCHAR(100),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE INDEX idx_transactions_payment_id ON transactions(payment_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
