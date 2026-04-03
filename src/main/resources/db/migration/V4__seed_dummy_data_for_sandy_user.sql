-- Insert dummy data for user: sandy007sandipan@gmail.com
-- This script adds the same set of dummy data for a new user (user_id = 3)

-- First, check if the user exists, if not create them
-- Note: Password is bcrypt hashed version of 'password123'
INSERT INTO users (email, password_hash, first_name, last_name, is_active, created_at, updated_at) 
VALUES ('sandy007sandipan@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sandipan', 'Sarkar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Insert sample transactions for Sandy (user_id = 3)
INSERT INTO transactions (user_id, category_id, amount, type, description, transaction_date, is_recurring_instance, created_at, updated_at)
SELECT 
    u.id,
    v.category_id,
    v.amount,
    v.type,
    v.description,
    v.transaction_date::date,
    v.is_recurring_instance,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
CROSS JOIN (
    VALUES 
    (5, 85.50, 'EXPENSE', 'Grocery shopping at Whole Foods', '2024-03-15', false),
    (1, 5500.00, 'INCOME', 'Monthly salary', '2024-03-01', false),
    (6, 45.00, 'EXPENSE', 'Gas station fill-up', '2024-03-14', false),
    (9, 120.00, 'EXPENSE', 'Electricity bill', '2024-03-10', false),
    (2, 1500.00, 'INCOME', 'Freelance web development project', '2024-03-12', false),
    (7, 250.00, 'EXPENSE', 'New running shoes', '2024-03-08', false),
    (8, 35.00, 'EXPENSE', 'Movie tickets and popcorn', '2024-03-05', false),
    (10, 75.00, 'EXPENSE', 'Doctor visit copay', '2024-03-03', false),
    (13, 1800.00, 'EXPENSE', 'Monthly rent payment', '2024-03-01', false),
    (11, 200.00, 'EXPENSE', 'Online course subscription', '2024-03-02', false)
) AS v(category_id, amount, type, description, transaction_date, is_recurring_instance)
WHERE u.email = 'sandy007sandipan@gmail.com'
AND NOT EXISTS (
    SELECT 1 FROM transactions t 
    WHERE t.user_id = u.id 
    AND t.category_id = v.category_id 
    AND t.transaction_date = v.transaction_date::date
);

-- Insert sample recurring transactions for Sandy
INSERT INTO recurring_transactions (user_id, category_id, name, amount, type, description, frequency, day_of_month, start_date, end_date, next_execution_date, is_active, created_at, updated_at)
SELECT 
    u.id,
    v.category_id,
    v.name,
    v.amount,
    v.type,
    v.description,
    v.frequency,
    v.day_of_month,
    v.start_date::date,
    v.end_date::date,
    v.next_execution_date::date,
    v.is_active,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
CROSS JOIN (
    VALUES 
    (1, 'Monthly Salary', 5500.00, 'INCOME', 'Regular monthly salary from employer', 'MONTHLY', 1, '2024-01-01', NULL, '2024-04-01', true),
    (13, 'Rent Payment', 1800.00, 'EXPENSE', 'Monthly apartment rent', 'MONTHLY', 1, '2024-01-01', NULL, '2024-04-01', true),
    (9, 'Internet Bill', 79.99, 'EXPENSE', 'High-speed internet subscription', 'MONTHLY', 15, '2024-01-01', NULL, '2024-03-15', true),
    (9, 'Netflix Subscription', 15.99, 'EXPENSE', 'Streaming service subscription', 'MONTHLY', 10, '2024-01-01', NULL, '2024-03-10', true),
    (3, 'Dividend Income', 250.00, 'INCOME', 'Quarterly dividend from investments', 'MONTHLY', 20, '2024-01-01', NULL, '2024-03-20', true),
    (11, 'Gym Membership', 49.99, 'EXPENSE', 'Monthly gym membership fee', 'MONTHLY', 5, '2024-01-01', NULL, '2024-03-05', true)
) AS v(category_id, name, amount, type, description, frequency, day_of_month, start_date, end_date, next_execution_date, is_active)
WHERE u.email = 'sandy007sandipan@gmail.com'
AND NOT EXISTS (
    SELECT 1 FROM recurring_transactions rt 
    WHERE rt.user_id = u.id 
    AND rt.category_id = v.category_id 
    AND rt.name = v.name
);

-- Insert sample budgets for Sandy
INSERT INTO budgets (user_id, name, limit_amount, period, alert_threshold, start_date, created_at, updated_at)
SELECT 
    u.id,
    v.name,
    v.limit_amount,
    v.period,
    v.alert_threshold,
    v.start_date::date,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
CROSS JOIN (
    VALUES 
    ('Monthly Food Budget', 600.00, 'MONTHLY', 0.80, '2024-01-01'),
    ('Entertainment Budget', 200.00, 'MONTHLY', 0.80, '2024-01-01'),
    ('Shopping Budget', 400.00, 'MONTHLY', 0.80, '2024-01-01'),
    ('Transportation Budget', 300.00, 'MONTHLY', 0.80, '2024-01-01'),
    ('Annual Travel Fund', 3000.00, 'YEARLY', 0.80, '2024-01-01')
) AS v(name, limit_amount, period, alert_threshold, start_date)
WHERE u.email = 'sandy007sandipan@gmail.com'
AND NOT EXISTS (
    SELECT 1 FROM budgets b 
    WHERE b.user_id = u.id 
    AND b.name = v.name
);

-- Link budgets to categories for Sandy
INSERT INTO budget_categories (budget_id, category_id, created_at, updated_at)
SELECT 
    b.id,
    v.category_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM budgets b
CROSS JOIN (
    VALUES 
    ('Monthly Food Budget', 5),
    ('Entertainment Budget', 8),
    ('Shopping Budget', 7),
    ('Transportation Budget', 6),
    ('Annual Travel Fund', 12)
) AS v(budget_name, category_id)
JOIN users u ON u.id = b.user_id
WHERE u.email = 'sandy007sandipan@gmail.com'
AND b.name = v.budget_name
AND NOT EXISTS (
    SELECT 1 FROM budget_categories bc 
    WHERE bc.budget_id = b.id 
    AND bc.category_id = v.category_id
);

-- Insert sample alerts for Sandy
INSERT INTO alerts (user_id, type, message, related_entity_type, related_entity_id, is_read, created_at, updated_at)
SELECT 
    u.id,
    v.type,
    v.message,
    v.related_entity_type,
    v.related_entity_id,
    v.is_read,
    v.created_at,
    v.updated_at
FROM users u
CROSS JOIN (
    VALUES 
    ('BUDGET_WARNING', 'Your Shopping budget is at 92% of the limit. Current spent: $368.00 of $400.00', 'BUDGET', 3, false, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('BUDGET_EXCEEDED', 'Your Shopping budget has been exceeded! Current spent: $450.00 of $400.00 limit', 'BUDGET', 3, false, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('RECURRING_TRANSACTION', 'Recurring transaction ''Rent Payment'' of $1,800.00 was generated for March 2024', 'RECURRING_TRANSACTION', 2, true, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days'),
    ('BUDGET_WARNING', 'Your Food & Dining budget is at 80% of the limit. Current spent: $480.00 of $600.00', 'BUDGET', 1, true, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('RECURRING_TRANSACTION', 'Recurring transaction ''Internet Bill'' of $79.99 was generated for March 2024', 'RECURRING_TRANSACTION', 3, true, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('HIGH_EXPENSE', 'Large expense detected: $1,800.00 for ''Monthly rent payment'' in Housing category', 'TRANSACTION', 9, false, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days')
) AS v(type, message, related_entity_type, related_entity_id, is_read, created_at, updated_at)
WHERE u.email = 'sandy007sandipan@gmail.com';

-- Display summary of inserted data for Sandy user
SELECT 'User sandy007sandipan@gmail.com data inserted successfully!' as status;