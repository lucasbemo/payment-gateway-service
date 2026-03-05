-- V11__add_missing_columns_to_customers_table.sql
ALTER TABLE customers ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL DEFAULT 'Unknown';
ALTER TABLE customers ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';

-- Drop the token column if not needed
-- ALTER TABLE customers DROP COLUMN IF EXISTS token;
