-- V17__add_status_to_payment_methods.sql
-- payment_methods.status was created in V12 as SMALLINT DEFAULT 0 and was written
-- by JPA using the default ORDINAL enum mapping. Convert it to readable VARCHAR(20)
-- status names ('ACTIVE', 'INACTIVE', ...) so soft-deleted rows are explicit and the
-- entity can use a stable STRING enum mapping.
-- Ordinal positions follow the infrastructure PaymentMethodStatus enum:
-- 0=ACTIVE, 1=INACTIVE, 2=EXPIRED, 3=SUSPENDED, 4=PENDING_VERIFICATION,
-- 5=VERIFIED, 6=FAILED_VERIFICATION, 7=REVOKED.
ALTER TABLE payment_methods ALTER COLUMN status DROP DEFAULT;
ALTER TABLE payment_methods ALTER COLUMN status TYPE VARCHAR(20) USING (
    CASE status
        WHEN 0 THEN 'ACTIVE'
        WHEN 1 THEN 'INACTIVE'
        WHEN 2 THEN 'EXPIRED'
        WHEN 3 THEN 'SUSPENDED'
        WHEN 4 THEN 'PENDING_VERIFICATION'
        WHEN 5 THEN 'VERIFIED'
        WHEN 6 THEN 'FAILED_VERIFICATION'
        WHEN 7 THEN 'REVOKED'
        ELSE 'ACTIVE'
    END
);
ALTER TABLE payment_methods ALTER COLUMN status SET DEFAULT 'ACTIVE';
ALTER TABLE payment_methods ALTER COLUMN status SET NOT NULL;

CREATE INDEX idx_payment_methods_status ON payment_methods(status);
