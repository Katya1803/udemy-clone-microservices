@echo off
set SERVICE=%1

if "%SERVICE%"=="" (
    echo Usage: rebuild-one service-name
    exit /b
)

echo ===============================
echo   BUILDING %SERVICE%
echo ===============================
docker compose build %SERVICE%

echo ===============================
echo   STARTING %SERVICE%
echo ===============================
docker compose up -d %SERVICE%

echo Done.
