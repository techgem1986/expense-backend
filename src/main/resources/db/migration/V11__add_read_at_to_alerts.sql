-- Add read_at column to alerts table
-- Required by the Alert JPA entity for tracking when alerts were read
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS read_at TIMESTAMP WITH TIME ZONE;