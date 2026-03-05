-- V8__create_reconciliation_tables.sql
CREATE TABLE reconciliation_batches (
    id VARCHAR(36) PRIMARY KEY,
    batch_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_transactions INTEGER NOT NULL DEFAULT 0,
    matched_transactions INTEGER NOT NULL DEFAULT 0,
    mismatched_transactions INTEGER NOT NULL DEFAULT 0,
    expected_amount DECIMAL(19,4),
    actual_amount DECIMAL(19,4),
    discrepancy_count INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reconciliation_batches_date UNIQUE (batch_date)
);

CREATE TABLE discrepancies (
    id VARCHAR(36) PRIMARY KEY,
    reconciliation_batch_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(36),
    payment_id VARCHAR(36) NOT NULL,
    discrepancy_type VARCHAR(100) NOT NULL,
    description TEXT,
    expected_amount DECIMAL(19,4),
    actual_amount DECIMAL(19,4),
    expected_status VARCHAR(50),
    actual_status VARCHAR(50),
    resolution_status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    resolution_notes TEXT,
    resolved_by VARCHAR(36),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_discrepancies_batch FOREIGN KEY (reconciliation_batch_id) REFERENCES reconciliation_batches(id),
    CONSTRAINT fk_discrepancies_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE TABLE settlement_reports (
    id VARCHAR(36) PRIMARY KEY,
    reconciliation_batch_id VARCHAR(36) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    report_format VARCHAR(20) NOT NULL,
    report_path VARCHAR(512),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    generated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settlement_reports_batch FOREIGN KEY (reconciliation_batch_id) REFERENCES reconciliation_batches(id)
);

CREATE INDEX idx_reconciliation_batches_status ON reconciliation_batches(status);
CREATE INDEX idx_reconciliation_batches_date ON reconciliation_batches(batch_date);
CREATE INDEX idx_discrepancies_batch_id ON discrepancies(reconciliation_batch_id);
CREATE INDEX idx_discrepancies_resolution_status ON discrepancies(resolution_status);
CREATE INDEX idx_settlement_reports_batch_id ON settlement_reports(reconciliation_batch_id);
