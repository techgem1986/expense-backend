-- Flush all data and insert dummy data for testing
-- This script will:
-- 1. Disable foreign key constraints
-- 2. Delete all data from tables in correct order
-- 3. Re-enable foreign key constraints
-- 4. Reset sequences
-- 5. Insert fresh dummy data

-- Step 1: Disable foreign key constraints temporarily
SET session_replication_role = 'replica';

-- Step 2: Delete all data (order matters due to foreign keys)
DELETE FROM alerts;
DELETE FROM budget_categories;
DELETE FROM budgets;
DELETE FROM transactions;
DELETE FROM recurring_transactions;
DELETE FROM categories;
DELETE FROM users;

-- Step 3: Re-enable foreign key constraints
SET session_replication_role = 'origin';

-- Step 4: Reset sequences to start fresh
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE categories_id_seq RESTART WITH 1;
ALTER SEQUENCE transactions_id_seq RESTART WITH 1;
ALTER SEQUENCE recurring_transactions_id_seq RESTART WITH 1;
ALTER SEQUENCE budgets_id_seq RESTART WITH 1;
ALTER SEQUENCE alerts_id_seq RESTART WITH 1;

-- Step 5: Insert dummy data

-- Insert test users
-- Note: Password is bcrypt hashed version of '$Passw0rd$'
INSERT INTO users (email, password_hash, first_name, last_name, is_active, created_at, updated_at) VALUES
('john.doe@example.com', '$2b$12$TkXhAWZ7MH0LQUj6xYZNAOUQLHuVVWQ1XMcrz1//QvrSkUNBDFZAS', 'John', 'Doe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jane.smith@example.com', '$2b$12$TkXhAWZ7MH0LQUj6xYZNAOUQLHuVVWQ1XMcrz1//QvrSkUNBDFZAS', 'Jane', 'Smith', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert categories (these are also in V1__init.sql, so we delete and re-insert to ensure clean state)
INSERT INTO categories (name, description, type, created_at, updated_at) VALUES
-- Income Categories
('Salary', 'Regular salary income', 'INCOME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Freelance', 'Freelance or contract work', 'INCOME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Investment', 'Investment returns or dividends', 'INCOME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Other Income', 'Miscellaneous income sources', 'INCOME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Expense Categories
('Food & Dining', 'Restaurants, groceries, food delivery', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Transportation', 'Gas, public transport, car maintenance', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Shopping', 'Clothing, electronics, general purchases', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Entertainment', 'Movies, games, hobbies', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bills & Utilities', 'Electricity, water, internet, phone', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Healthcare', 'Medical expenses, insurance', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Education', 'Courses, books, training', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Travel', 'Vacations, business trips', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Housing', 'Rent, mortgage, home maintenance', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Other Expense', 'Miscellaneous expenses', 'EXPENSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample transactions for user 1 (John Doe)
INSERT INTO transactions (user_id, category_id, amount, type, description, transaction_date, is_recurring_instance, created_at, updated_at) VALUES
(1, 5, 85.50, 'EXPENSE', 'Grocery shopping at Whole Foods', '2024-03-15', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 5500.00, 'INCOME', 'Monthly salary', '2024-03-01', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 6, 45.00, 'EXPENSE', 'Gas station fill-up', '2024-03-14', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 9, 120.00, 'EXPENSE', 'Electricity bill', '2024-03-10', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, 1500.00, 'INCOME', 'Freelance web development project', '2024-03-12', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 7, 250.00, 'EXPENSE', 'New running shoes', '2024-03-08', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 8, 35.00, 'EXPENSE', 'Movie tickets and popcorn', '2024-03-05', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 10, 75.00, 'EXPENSE', 'Doctor visit copay', '2024-03-03', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 13, 1800.00, 'EXPENSE', 'Monthly rent payment', '2024-03-01', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 11, 200.00, 'EXPENSE', 'Online course subscription', '2024-03-02', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample recurring transactions for user 1
INSERT INTO recurring_transactions (user_id, category_id, name, amount, type, description, frequency, day_of_month, start_date, end_date, next_execution_date, is_active, created_at, updated_at) VALUES
(1, 1, 'Monthly Salary', 5500.00, 'INCOME', 'Regular monthly salary from employer', 'MONTHLY', 1, '2024-01-01', NULL, '2024-04-01', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 13, 'Rent Payment', 1800.00, 'EXPENSE', 'Monthly apartment rent', 'MONTHLY', 1, '2024-01-01', NULL, '2024-04-01', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 9, 'Internet Bill', 79.99, 'EXPENSE', 'High-speed internet subscription', 'MONTHLY', 15, '2024-01-01', NULL, '2024-03-15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 9, 'Netflix Subscription', 15.99, 'EXPENSE', 'Streaming service subscription', 'MONTHLY', 10, '2024-01-01', NULL, '2024-03-10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 3, 'Dividend Income', 250.00, 'INCOME', 'Quarterly dividend from investments', 'MONTHLY', 20, '2024-01-01', NULL, '2024-03-20', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 11, 'Gym Membership', 49.99, 'EXPENSE', 'Monthly gym membership fee', 'MONTHLY', 5, '2024-01-01', NULL, '2024-03-05', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample budgets for user 1
INSERT INTO budgets (user_id, name, limit_amount, period, alert_threshold, start_date, created_at, updated_at) VALUES
(1, 'Monthly Food Budget', 600.00, 'MONTHLY', 0.80, '2024-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Entertainment Budget', 200.00, 'MONTHLY', 0.80, '2024-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Shopping Budget', 400.00, 'MONTHLY', 0.80, '2024-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Transportation Budget', 300.00, 'MONTHLY', 0.80, '2024-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Annual Travel Fund', 3000.00, 'YEARLY', 0.80, '2024-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Link budgets to categories (budget_categories now has id, created_at, updated_at columns from JPA)
INSERT INTO budget_categories (budget_id, category_id, created_at, updated_at) VALUES
(1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- Food Budget -> Food & Dining
(2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- Entertainment Budget -> Entertainment
(3, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- Shopping Budget -> Shopping
(4, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- Transportation Budget -> Transportation
(5, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- Travel Fund -> Travel

-- Insert sample alerts for user 1
INSERT INTO alerts (user_id, type, message, related_entity_type, related_entity_id, is_read, created_at, updated_at) VALUES
(1, 'BUDGET_WARNING', 'Your Shopping budget is at 92% of the limit. Current spent: $368.00 of $400.00', 'BUDGET', 3, false, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 'BUDGET_EXCEEDED', 'Your Shopping budget has been exceeded! Current spent: $450.00 of $400.00 limit', 'BUDGET', 3, false, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(1, 'RECURRING_TRANSACTION', 'Recurring transaction ''Rent Payment'' of $1,800.00 was generated for March 2024', 'RECURRING_TRANSACTION', 2, true, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days'),
(1, 'BUDGET_WARNING', 'Your Food & Dining budget is at 80% of the limit. Current spent: $480.00 of $600.00', 'BUDGET', 1, true, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(1, 'RECURRING_TRANSACTION', 'Recurring transaction ''Internet Bill'' of $79.99 was generated for March 2024', 'RECURRING_TRANSACTION', 3, true, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 'HIGH_EXPENSE', 'Large expense detected: $1,800.00 for ''Monthly rent payment'' in Housing category', 'TRANSACTION', 9, false, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days');

-- Display summary of inserted data
SELECT 'Users inserted: ' || COUNT(*) as summary FROM users;
SELECT 'Categories inserted: ' || COUNT(*) as summary FROM categories;
SELECT 'Transactions inserted: ' || COUNT(*) as summary FROM transactions;
SELECT 'Recurring Transactions inserted: ' || COUNT(*) as summary FROM recurring_transactions;
SELECT 'Budgets inserted: ' || COUNT(*) as summary FROM budgets;
SELECT 'Budget Categories inserted: ' || COUNT(*) as summary FROM budget_categories;
SELECT 'Alerts inserted: ' || COUNT(*) as summary FROM alerts;