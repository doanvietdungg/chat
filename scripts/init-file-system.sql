-- Script to initialize/update file system with polymorphic associations
-- Run this script if you need to manually update existing database

-- ============================================
-- STEP 1: Add new columns if not exists
-- ============================================

DO $$ 
BEGIN
    -- Add owner_type column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'files' AND column_name = 'owner_type'
    ) THEN
        ALTER TABLE files ADD COLUMN owner_type VARCHAR(30);
        RAISE NOTICE 'Added owner_type column';
    ELSE
        RAISE NOTICE 'owner_type column already exists';
    END IF;

    -- Add owner_id column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'files' AND column_name = 'owner_id'
    ) THEN
        ALTER TABLE files ADD COLUMN owner_id UUID;
        RAISE NOTICE 'Added owner_id column';
    ELSE
        RAISE NOTICE 'owner_id column already exists';
    END IF;
END $$;

-- ============================================
-- STEP 2: Set default values for existing files
-- ============================================

-- Set NONE for files without owner
UPDATE files 
SET owner_type = 'NONE' 
WHERE owner_type IS NULL;

RAISE NOTICE 'Set default owner_type = NONE for % files', 
    (SELECT COUNT(*) FROM files WHERE owner_type = 'NONE' AND owner_id IS NULL);

-- ============================================
-- STEP 3: Migrate existing file-message relationships
-- ============================================

-- Update files that are referenced by messages
WITH updated_files AS (
    UPDATE files f
    SET owner_type = 'MESSAGE',
        owner_id = m.id
    FROM messages m
    WHERE m.file_id = f.id
      AND (f.owner_type = 'NONE' OR f.owner_type IS NULL)
    RETURNING f.id
)
SELECT COUNT(*) AS migrated_files FROM updated_files;

-- ============================================
-- STEP 4: Create indexes for performance
-- ============================================

-- Index for polymorphic queries
CREATE INDEX IF NOT EXISTS idx_files_owner 
ON files(owner_type, owner_id);

-- Index for uploaded_by queries
CREATE INDEX IF NOT EXISTS idx_files_uploaded_by 
ON files(uploaded_by);

-- Index for created_at (for sorting)
CREATE INDEX IF NOT EXISTS idx_files_created_at 
ON files(created_at DESC);

-- ============================================
-- STEP 5: Add constraints and comments
-- ============================================

-- Add check constraint for valid owner types
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'files_owner_type_check'
    ) THEN
        ALTER TABLE files 
        ADD CONSTRAINT files_owner_type_check 
        CHECK (owner_type IN ('MESSAGE', 'POST', 'PROFILE', 'CHAT', 'COMMENT', 'NONE'));
        RAISE NOTICE 'Added owner_type check constraint';
    END IF;
END $$;

-- Add comments
COMMENT ON COLUMN files.owner_type IS 'Type of entity that owns this file: MESSAGE, POST, PROFILE, CHAT, COMMENT, NONE';
COMMENT ON COLUMN files.owner_id IS 'ID of the owner entity (polymorphic association)';
COMMENT ON TABLE files IS 'File storage with polymorphic associations - files can belong to different entity types';

-- ============================================
-- STEP 6: Verification queries
-- ============================================

-- Show statistics
SELECT 
    owner_type,
    COUNT(*) AS file_count,
    SUM(size) AS total_size_bytes,
    ROUND(SUM(size)::numeric / 1024 / 1024, 2) AS total_size_mb
FROM files
GROUP BY owner_type
ORDER BY file_count DESC;

-- Show files without owner
SELECT 
    id,
    name,
    size,
    content_type,
    uploaded_by,
    created_at
FROM files
WHERE owner_type = 'NONE'
ORDER BY created_at DESC
LIMIT 10;

-- Show message files
SELECT 
    f.id AS file_id,
    f.name AS file_name,
    f.owner_type,
    m.id AS message_id,
    m.text AS message_text,
    m.type AS message_type
FROM files f
JOIN messages m ON f.owner_id = m.id
WHERE f.owner_type = 'MESSAGE'
ORDER BY f.created_at DESC
LIMIT 10;

-- ============================================
-- STEP 7: Cleanup orphaned files (optional)
-- ============================================

-- Find orphaned files (files with MESSAGE owner but message doesn't exist)
SELECT 
    f.id,
    f.name,
    f.owner_type,
    f.owner_id,
    f.created_at
FROM files f
WHERE f.owner_type = 'MESSAGE'
  AND NOT EXISTS (
      SELECT 1 FROM messages m WHERE m.id = f.owner_id
  );

-- Uncomment to delete orphaned files
-- DELETE FROM files
-- WHERE owner_type = 'MESSAGE'
--   AND NOT EXISTS (
--       SELECT 1 FROM messages m WHERE m.id = owner_id
--   );

RAISE NOTICE 'File system initialization completed successfully!';
