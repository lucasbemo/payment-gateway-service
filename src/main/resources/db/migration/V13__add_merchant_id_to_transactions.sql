-- V13__add_merchant_id_to_transactions.sql
ALTER TABLE transactions ADD COLUMN merchant_id VARCHAR(36);
ALTER TABLE transactions ADD COLUMN net_amount DECIMAL(19,4);
ALTER TABLE transactions ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;

-- Add merchant_id and refund_idempotency_key to refunds
ALTER TABLE refunds ADD COLUMN merchant_id VARCHAR(36);
ALTER TABLE refunds ADD COLUMN refund_idempotency_key VARCHAR(255);
ALTER TABLE refunds ADD COLUMN refunded_amount DECIMAL(19,4);
ALTER TABLE refunds ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;

CREATE INDEX idx_refunds_idempotency_key ON refunds(refund_idempotency_key);

-- Add missing columns to reconciliation_batches for domain model alignment
ALTER TABLE reconciliation_batches ADD COLUMN merchant_id VARCHAR(36);
ALTER TABLE reconciliation_batches ADD COLUMN gateway_name VARCHAR(100);
ALTER TABLE reconciliation_batches ADD COLUMN initiated_by VARCHAR(100);

-- Add missing columns to settlement_reports for domain model alignment
ALTER TABLE settlement_reports ADD COLUMN merchant_id VARCHAR(36);
ALTER TABLE settlement_reports ADD COLUMN gateway_name VARCHAR(100);
ALTER TABLE settlement_reports ADD COLUMN settlement_date DATE;
ALTER TABLE settlement_reports ADD COLUMN gateway_report_id VARCHAR(255);
ALTER TABLE settlement_reports ADD COLUMN gross_amount DECIMAL(19,4);
ALTER TABLE settlement_reports ADD COLUMN fee_amount DECIMAL(19,4);
ALTER TABLE settlement_reports ADD COLUMN net_amount DECIMAL(19,4);
ALTER TABLE settlement_reports ADD COLUMN currency VARCHAR(3);
ALTER TABLE settlement_reports ADD COLUMN transaction_count INTEGER;
ALTER TABLE settlement_reports ADD COLUMN settled_at TIMESTAMP;
