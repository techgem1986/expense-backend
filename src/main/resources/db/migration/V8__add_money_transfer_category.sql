-- Add Money Transfer category for account-to-account transfers
-- This category is used to identify transactions that move money between accounts
INSERT INTO categories (name, description, type) VALUES
('Money Transfer', 'Transfer between accounts', 'EXPENSE')
ON CONFLICT (name) DO NOTHING;