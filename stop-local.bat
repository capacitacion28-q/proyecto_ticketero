@echo off
echo ========================================
echo    DETENIENDO PROYECTO TICKETERO LOCAL
echo ========================================

echo.
echo [1/2] Deteniendo contenedores Docker...
docker-compose -f docker-compose.dev.yml down

echo [2/2] Limpiando procesos Java y Node...
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im node.exe >nul 2>&1

echo.
echo ========================================
echo    PROYECTO DETENIDO EXITOSAMENTE
echo ========================================
echo.
pause