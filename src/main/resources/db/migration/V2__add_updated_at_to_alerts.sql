-- Add updated_at column to alerts table for JPA auditing
-- This migration adds the missing updated_at column required by BaseEntity

ALTER TABLE alerts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have a default updated_at value
UPDATE alerts SET updated_at = created_at WHERE updated_at IS NULL;