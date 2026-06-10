-- Add external_id column so merchant-provided customer identifiers are persisted
ALTER TABLE customers ADD COLUMN external_id VARCHAR(255);

CREATE INDEX idx_customers_merchant_external_id ON customers (merchant_id, external_id);
