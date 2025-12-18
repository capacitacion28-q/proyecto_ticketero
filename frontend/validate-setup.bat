@echo off
echo ========================================
echo Validando Setup del Frontend Ticketero
echo ========================================

echo.
echo 1. Verificando Node.js...
node --version
if %errorlevel% neq 0 (
    echo ERROR: Node.js no está instalado
    echo Instalar desde: https://nodejs.org/
    pause
    exit /b 1
)

echo.
echo 2. Verificando npm...
npm --version
if %errorlevel% neq 0 (
    echo ERROR: npm no está disponible
    pause
    exit /b 1
)

echo.
echo 3. Instalando dependencias...
npm install
if %errorlevel% neq 0 (
    echo ERROR: Falló la instalación de dependencias
    pause
    exit /b 1
)

echo.
echo 4. Verificando configuración TypeScript...
npm run check
if %errorlevel% neq 0 (
    echo ERROR: Falló la verificación de TypeScript
    pause
    exit /b 1
)

echo.
echo 5. Compilando proyecto...
npm run build
if %errorlevel% neq 0 (
    echo ERROR: Falló la compilación
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ SETUP COMPLETADO EXITOSAMENTE
echo ========================================
echo.
echo Para iniciar el servidor de desarrollo:
echo npm run dev
echo.
pause