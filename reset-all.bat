@echo off
echo ===============================
echo   SHUTTING DOWN ALL
echo ===============================
docker compose down

echo ===============================
echo   REBUILDING AND STARTING ALL
echo ===============================
docker compose up -d --build

echo Done.
