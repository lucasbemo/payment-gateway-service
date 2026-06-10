-- Settlement reports can be generated ad-hoc for a merchant/date range without a
-- reconciliation batch; the FK still applies when a batch id is present.
ALTER TABLE settlement_reports ALTER COLUMN reconciliation_batch_id DROP NOT NULL;
