#!/bin/bash
# Script to initialize file system with polymorphic associations
# Run this script to update existing database

echo "========================================"
echo "File System Initialization Script"
echo "========================================"
echo ""

# Set database connection parameters
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-jace_chat}
DB_USER=${DB_USER:-postgres}

echo "Database: $DB_NAME"
echo "Host: $DB_HOST:$DB_PORT"
echo "User: $DB_USER"
echo ""

# Prompt for password
read -sp "Enter database password: " DB_PASSWORD
export PGPASSWORD=$DB_PASSWORD
echo ""
echo ""

echo "Running migration script..."
echo ""

# Run the SQL script
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f init-file-system.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Migration completed successfully!"
    echo "========================================"
else
    echo ""
    echo "========================================"
    echo "Migration failed with error code: $?"
    echo "========================================"
fi

# Clear password from environment
unset PGPASSWORD

echo ""
