-- PostgreSQL Initialization Script for Payment Gateway Service
-- This script runs on first database startup

\echo 'Initializing Payment Gateway database...'

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- For text similarity searches
CREATE EXTENSION IF NOT EXISTS "btree_gin"; -- For composite index support

-- Set timezone
SET timezone = 'UTC';

\echo 'Database extensions created successfully!'

-- Grant privileges (for non-superuser connections)
-- Note: Actual user creation should be done via environment variables
-- DO NOT create users here in production

\echo 'Database initialization complete!'
