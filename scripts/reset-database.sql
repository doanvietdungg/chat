-- Script to reset database and reload sample data
-- Run this in PostgreSQL to completely reset the database

-- Drop all tables (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS message_reads CASCADE;
DROP TABLE IF EXISTS reactions CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS search_history CASCADE;
DROP TABLE IF EXISTS contacts CASCADE;
DROP TABLE IF EXISTS user_presence CASCADE;
DROP TABLE IF EXISTS files CASCADE;
DROP TABLE IF EXISTS chats CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop flyway schema history table to allow clean migration
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Note: After running this script, restart your Spring Boot application
-- Flyway will automatically run all migrations (V1 through V6) and recreate
-- all tables with sample data.