-- V12__create_payment_methods_table.sql
CREATE TABLE payment_methods (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    merchant_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    status SMALLINT NOT NULL DEFAULT 0,
    expiry_month VARCHAR(2),
    expiry_year VARCHAR(4),
    last_four VARCHAR(4),
    brand VARCHAR(50),
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_payment_methods_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_methods_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

CREATE INDEX idx_payment_methods_customer_id ON payment_methods(customer_id);
CREATE INDEX idx_payment_methods_merchant_id ON payment_methods(merchant_id);
CREATE INDEX idx_payment_methods_token ON payment_methods(token);
