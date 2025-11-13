@echo off
echo ===============================
echo   BUILDING ALL SERVICES
echo ===============================
docker compose build

echo ===============================
echo   STARTING ALL SERVICES
echo ===============================
docker compose up -d

echo Done.
