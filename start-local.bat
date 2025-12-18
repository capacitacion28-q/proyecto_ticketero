@echo off
echo ========================================
echo    INICIANDO PROYECTO TICKETERO LOCAL
echo ========================================

echo.
echo [1/4] Verificando Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker no esta instalado o no esta en PATH
    echo Instala Docker Desktop desde: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo [2/4] Iniciando Base de Datos PostgreSQL...
docker-compose -f docker-compose.dev.yml up -d postgres
if %errorlevel% neq 0 (
    echo ERROR: No se pudo iniciar PostgreSQL
    pause
    exit /b 1
)

echo Esperando que PostgreSQL este listo...
timeout /t 10 /nobreak >nul

echo [3/4] Iniciando API Spring Boot...
start "Ticketero API" cmd /k "mvn spring-boot:run"

echo Esperando que la API este lista...
timeout /t 15 /nobreak >nul

echo [4/4] Iniciando Frontend Svelte...
cd frontend
start "Ticketero Frontend" cmd /k "npm run dev"
cd ..

echo.
echo ========================================
echo    PROYECTO INICIADO EXITOSAMENTE
echo ========================================
echo.
echo URLs disponibles:
echo   Frontend: http://localhost:5173
echo   API:      http://localhost:8081
echo   Admin:    http://localhost:5173/admin
echo   Totem:    http://localhost:5173/totem
echo.
echo Para detener todo:
echo   1. Cierra las ventanas de API y Frontend
echo   2. Ejecuta: docker-compose -f docker-compose.dev.yml down
echo.
pause