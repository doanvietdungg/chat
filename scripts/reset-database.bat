@echo off
REM Script to reset PostgreSQL database and reload sample data
REM Usage: reset-database.bat [database_name] [username] [host] [port]

set DB_NAME=%1
set DB_USER=%2
set DB_HOST=%3
set DB_PORT=%4

if "%DB_NAME%"=="" set DB_NAME=chatdb
if "%DB_USER%"=="" set DB_USER=postgres
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=5432

echo Resetting database: %DB_NAME%
echo Host: %DB_HOST%:%DB_PORT%
echo User: %DB_USER%
echo.

REM Prompt for password
set /p DB_PASSWORD=Enter PostgreSQL password for user %DB_USER%: 

REM Set password environment variable
set PGPASSWORD=%DB_PASSWORD%

echo Dropping all tables and flyway history...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f reset-database.sql

if %ERRORLEVEL% EQU 0 (
    echo ✅ Database reset successfully!
    echo.
    echo Next steps:
    echo 1. Restart your Spring Boot application
    echo 2. Flyway will automatically run migrations V1-V6
    echo 3. Sample data will be loaded automatically
    echo.
    echo Test users ^(password: 123456^):
    echo - admin@chat.com
    echo - test1@chat.com
    echo - test2@chat.com
    echo - demo@chat.com
) else (
    echo ❌ Database reset failed!
    pause
    exit /b 1
)

REM Clear password from environment
set PGPASSWORD=
pause