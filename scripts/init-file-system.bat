@echo off
REM Script to initialize file system with polymorphic associations
REM Run this script to update existing database

echo ========================================
echo File System Initialization Script
echo ========================================
echo.

REM Set database connection parameters
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=jace_chat
set DB_USER=postgres

echo Database: %DB_NAME%
echo Host: %DB_HOST%:%DB_PORT%
echo User: %DB_USER%
echo.

REM Prompt for password
set /p DB_PASSWORD="Enter database password: "
echo.

echo Running migration script...
echo.

REM Run the SQL script
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f init-file-system.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Migration completed successfully!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Migration failed with error code: %ERRORLEVEL%
    echo ========================================
)

echo.
pause
