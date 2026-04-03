-- Add accounts table for managing user financial accounts
-- Accounts can be savings, loans, investments, mutual funds, etc.

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL CHECK (account_type IN ('SAVINGS', 'CHECKING', 'LOAN', 'INVESTMENT', 'MUTUAL_FUND', 'CREDIT_CARD', 'CASH', 'OTHER')),
    bank_name VARCHAR(255),
    account_number VARCHAR(100),
    ifsc_code VARCHAR(20),
    opening_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (opening_balance >= 0),
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (current_balance >= 0),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add from_account_id and to_account_id columns to transactions table
ALTER TABLE transactions 
ADD COLUMN from_account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL;

ALTER TABLE transactions 
ADD COLUMN to_account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL;

-- Indexes for accounts
CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_user_active ON accounts(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_accounts_user_type ON accounts(user_id, account_type);

-- Indexes for transactions with accounts
CREATE INDEX IF NOT EXISTS idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account ON transactions(to_account_id);

-- Insert default Cash account for existing users (if any)
INSERT INTO accounts (user_id, name, account_type, opening_balance, current_balance, description, is_active)
SELECT id, 'Cash', 'CASH', 0.00, 0.00, 'Default cash account', TRUE
FROM users
WHERE NOT EXISTS (
    SELECT 1 FROM accounts a WHERE a.user_id = users.id AND a.account_type = 'CASH'
);