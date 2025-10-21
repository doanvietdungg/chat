#!/bin/bash

# Script to reset PostgreSQL database and reload sample data
# Usage: ./reset-database.sh [database_name] [username] [host] [port]

DB_NAME=${1:-chatdb}
DB_USER=${2:-postgres}
DB_HOST=${3:-localhost}
DB_PORT=${4:-5432}

echo "Resetting database: $DB_NAME"
echo "Host: $DB_HOST:$DB_PORT"
echo "User: $DB_USER"
echo ""

# Prompt for password
echo "Enter PostgreSQL password for user $DB_USER:"
read -s DB_PASSWORD

# Export password for psql
export PGPASSWORD=$DB_PASSWORD

echo "Dropping all tables and flyway history..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f reset-database.sql

if [ $? -eq 0 ]; then
    echo "✅ Database reset successfully!"
    echo ""
    echo "Next steps:"
    echo "1. Restart your Spring Boot application"
    echo "2. Flyway will automatically run migrations V1-V6"
    echo "3. Sample data will be loaded automatically"
    echo ""
    echo "Test users (password: 123456):"
    echo "- admin@chat.com"
    echo "- test1@chat.com"
    echo "- test2@chat.com"
    echo "- demo@chat.com"
else
    echo "❌ Database reset failed!"
    exit 1
fi

# Clear password from environment
unset PGPASSWORD