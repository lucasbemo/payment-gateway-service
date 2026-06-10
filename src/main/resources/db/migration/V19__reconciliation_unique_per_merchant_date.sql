-- V19__reconciliation_unique_per_merchant_date.sql
-- A reconciliation batch is unique per merchant per date, not per date globally.
-- The original UNIQUE(batch_date) constraint (V8) allowed only ONE reconciliation
-- batch system-wide for any given calendar date, so a second merchant reconciling
-- the same date failed with a duplicate-key error. Replace it with a per-merchant
-- uniqueness rule.
ALTER TABLE reconciliation_batches DROP CONSTRAINT IF EXISTS uk_reconciliation_batches_date;

ALTER TABLE reconciliation_batches
    ADD CONSTRAINT uk_reconciliation_batches_merchant_date UNIQUE (merchant_id, batch_date);
