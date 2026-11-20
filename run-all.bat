@echo off
title EcoCode Runner
echo ====================================================
echo               Starting EcoCode Platform
echo ====================================================
echo.

:: 1. Ensure Docker PostgreSQL is running
echo [1/3] Checking / Starting PostgreSQL in Docker...
docker-compose up -d db

:: 2. Start Backend in a new window
echo [2/3] Launching Spring Boot Backend (Port 8080)...
start "EcoCode Backend" cmd /k "start-backend.bat"

:: 3. Start Frontend in a new window
echo [3/3] Launching React Frontend (Port 3000)...
start "EcoCode Frontend" cmd /k "cd /d "%~dp0frontend" && npm run dev"

echo.
echo ====================================================
echo EcoCode is launching!
echo Frontend: http://localhost:3000
echo Backend:  http://localhost:8080
echo Database: localhost:5432
echo ====================================================
timeout /t 5 >nul
start http://localhost:3000
