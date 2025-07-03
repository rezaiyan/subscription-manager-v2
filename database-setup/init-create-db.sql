-- Initialize Create Subscription Service Database
-- Run this script to set up the subscription_create_db

-- Create database (if not exists)
-- Note: This should be run as a superuser or database owner

-- Create the create service database
CREATE DATABASE subscription_create_db;

-- Connect to the create service database
\c subscription_create_db;

-- Create subscriptions table
CREATE TABLE IF NOT EXISTS subscription (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    amount DECIMAL(10,2) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    next_billing_date TIMESTAMP,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_subscription_active ON subscription(active);
CREATE INDEX IF NOT EXISTS idx_subscription_frequency ON subscription(frequency);
CREATE INDEX IF NOT EXISTS idx_subscription_name ON subscription(name);

-- Create audit table for tracking creation events
CREATE TABLE IF NOT EXISTS subscription_audit (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on audit table
CREATE INDEX IF NOT EXISTS idx_subscription_audit_subscription_id ON subscription_audit(subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscription_audit_created_at ON subscription_audit(created_at); 