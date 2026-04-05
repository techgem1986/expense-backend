-- Add user_id column to categories table for user-specific category management

-- First, add the column as nullable
ALTER TABLE categories ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Update existing categories to associate them with a default user (user with id=1)
-- This ensures no NULL values before we add the NOT NULL constraint
UPDATE categories SET user_id = 1 WHERE user_id IS NULL;

-- Verify user with id=1 exists, if not create a default user
INSERT INTO users (email, password_hash, first_name, last_name) 
SELECT 'default@system.com', 'system', 'System', 'User'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

-- Make the user_id column NOT NULL (only if there are no NULL values)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM categories WHERE user_id IS NULL) THEN
        ALTER TABLE categories ALTER COLUMN user_id SET NOT NULL;
    END IF;
END $$;

-- Add foreign key constraint (only if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_category_user' AND table_name = 'categories'
    ) THEN
        ALTER TABLE categories 
        ADD CONSTRAINT fk_category_user 
        FOREIGN KEY (user_id) REFERENCES users(id);
    END IF;
END $$;

-- Create index for better query performance (only if it doesn't exist)
CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories(user_id);
