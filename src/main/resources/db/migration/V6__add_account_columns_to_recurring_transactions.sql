-- Add from_account_id and to_account_id columns to recurring_transactions table
ALTER TABLE recurring_transactions ADD COLUMN from_account_id BIGINT;
ALTER TABLE recurring_transactions ADD COLUMN to_account_id BIGINT;

-- Add foreign key constraints
ALTER TABLE recurring_transactions 
ADD CONSTRAINT fk_recurring_from_account 
FOREIGN KEY (from_account_id) REFERENCES accounts(id);

ALTER TABLE recurring_transactions 
ADD CONSTRAINT fk_recurring_to_account 
FOREIGN KEY (to_account_id) REFERENCES accounts(id);