-- V9__create_payment_items_table.sql
CREATE TABLE payment_items (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    description VARCHAR(512),
    quantity INT NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    total DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_items_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_items_payment_id ON payment_items(payment_id);
