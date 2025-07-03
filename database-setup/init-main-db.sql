-- Initialize Main Application Database
-- Run this script to set up the subscription_main_db

-- Create database (if not exists)
-- Note: This should be run as a superuser or database owner

-- Create the main database
CREATE DATABASE subscription_main_db;

-- Connect to the main database
\c subscription_main_db;

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

-- Insert sample data (optional)
INSERT INTO subscription (name, description, amount, frequency, start_date, active) VALUES
('Netflix', 'Streaming Service', 15.99, 'MONTHLY', NOW() - INTERVAL '30 days', true),
('Spotify', 'Music Streaming', 9.99, 'MONTHLY', NOW() - INTERVAL '60 days', true),
('Amazon Prime', 'Shopping + Video', 119.00, 'YEARLY', NOW() - INTERVAL '365 days', true),
('Adobe Creative Cloud', 'Design Tools', 52.99, 'MONTHLY', NOW() - INTERVAL '90 days', false)
ON CONFLICT DO NOTHING; 