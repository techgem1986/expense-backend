-- Update account types to new supported types:
-- SAVINGS, SALARY, CURRENT, INVESTMENT, MUTUAL_FUND, CREDIT_CARD

-- 1. Drop the old CHECK constraint and add the new one
ALTER TABLE accounts DROP CONSTRAINT IF EXISTS accounts_account_type_check;
ALTER TABLE accounts ADD CONSTRAINT accounts_account_type_check 
    CHECK (account_type IN ('SAVINGS', 'SALARY', 'CURRENT', 'INVESTMENT', 'MUTUAL_FUND', 'CREDIT_CARD'));

-- 2. Migrate existing data:
--    CHECKING -> CURRENT
--    LOAN -> CREDIT_CARD
--    CASH -> SAVINGS
--    OTHER -> SAVINGS
--    (MUTUAL_FUND and CREDIT_CARD already match, just need display name updated in code)
UPDATE accounts SET account_type = 'CURRENT' WHERE account_type = 'CHECKING';
UPDATE accounts SET account_type = 'CREDIT_CARD' WHERE account_type = 'LOAN';
UPDATE accounts SET account_type = 'SAVINGS' WHERE account_type = 'CASH';
UPDATE accounts SET account_type = 'SAVINGS' WHERE account_type = 'OTHER';