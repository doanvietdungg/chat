-- Add polymorphic association fields to files table
-- This allows files to belong to different entity types (MESSAGE, POST, PROFILE, etc.)

-- Add owner_type column
ALTER TABLE files 
ADD COLUMN IF NOT EXISTS owner_type VARCHAR(30);

-- Add owner_id column
ALTER TABLE files 
ADD COLUMN IF NOT EXISTS owner_id UUID;

-- Set default owner_type for existing files
UPDATE files 
SET owner_type = 'NONE' 
WHERE owner_type IS NULL;

-- Update existing files that are referenced by messages
UPDATE files f
SET owner_type = 'MESSAGE',
    owner_id = m.id
FROM messages m
WHERE m.file_id = f.id
  AND f.owner_type = 'NONE';

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_files_owner 
ON files(owner_type, owner_id);

-- Create index for uploaded_by
CREATE INDEX IF NOT EXISTS idx_files_uploaded_by 
ON files(uploaded_by);

-- Add comment to document the polymorphic pattern
COMMENT ON COLUMN files.owner_type IS 'Type of entity that owns this file: MESSAGE, POST, PROFILE, CHAT, COMMENT, NONE';
COMMENT ON COLUMN files.owner_id IS 'ID of the owner entity (polymorphic association)';
