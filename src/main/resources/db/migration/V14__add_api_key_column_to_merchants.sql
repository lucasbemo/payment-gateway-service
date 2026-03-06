-- V14__add_api_key_column_to_merchants.sql
-- Add plain api_key column for lookup (public identifier, not secret)
ALTER TABLE merchants ADD COLUMN api_key VARCHAR(64) UNIQUE;

-- Populate api_key for existing merchants (use a placeholder that will need to be regenerated)
-- In production, merchants would need to regenerate their API keys
UPDATE merchants SET api_key = CONCAT('key_', MD5(email || created_at::text)) WHERE api_key IS NULL;

-- Add index for faster API key lookups during authentication
CREATE INDEX idx_merchants_api_key ON merchants(api_key);

-- Add constraint to ensure api_key is not null
ALTER TABLE merchants ALTER COLUMN api_key SET NOT NULL;
