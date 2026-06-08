-- Make categories fully user-specific
-- 1. Drop the global UNIQUE constraint on name
-- 2. Add a composite UNIQUE constraint on (name, user_id)
-- 3. Reassign default user (user_id=1) categories to their respective users where possible
-- 4. Delete any remaining categories that don't belong to any user

-- Step 1: Drop the UNIQUE constraint on name
-- The constraint name may vary depending on how it was created
DO $$
BEGIN
    -- Try to drop the constraint if it exists (PostgreSQL auto-names it)
    IF EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'categories_name_key' AND conrelid = 'categories'::regclass
    ) THEN
        ALTER TABLE categories DROP CONSTRAINT categories_name_key;
    END IF;
END $$;

-- Step 2: Add composite UNIQUE constraint on (name, user_id)
-- This ensures users can't have duplicate category names, but different users can
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uq_categories_name_user' AND conrelid = 'categories'::regclass
    ) THEN
        -- First delete any duplicate name+user_id combinations keeping the first one
        DELETE FROM categories c1 USING categories c2
        WHERE c1.id > c2.id 
        AND c1.name = c2.name 
        AND c1.user_id = c2.user_id;
        
        ALTER TABLE categories ADD CONSTRAINT uq_categories_name_user UNIQUE (name, user_id);
    END IF;
END $$;

-- Step 3: Delete categories owned by user_id=1 (default/system user)
-- These should not be seeded anymore - each user creates their own
DELETE FROM categories WHERE user_id = 1;