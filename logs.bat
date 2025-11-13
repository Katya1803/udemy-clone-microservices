@echo off
set SERVICE=%1

if "%SERVICE%"=="" (
    echo Usage: logs service-name
    exit /b
)

docker compose logs -f %SERVICE%
