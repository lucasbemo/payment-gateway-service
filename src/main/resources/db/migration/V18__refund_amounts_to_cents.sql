-- V18__refund_amounts_to_cents.sql
-- Refund amounts were stored in currency UNITS (e.g. 50.0000 for a 5000-cent refund)
-- while payments and transactions store CENTS. Convert existing rows to the cents
-- convention used by the rest of the schema. DECIMAL(19,4) comfortably holds the
-- multiplied values. refunded_amount (added in V13) follows the same Money mapping
-- and must be converted together with amount (NULL * 100 stays NULL).
UPDATE refunds
SET amount = amount * 100,
    refunded_amount = refunded_amount * 100;
