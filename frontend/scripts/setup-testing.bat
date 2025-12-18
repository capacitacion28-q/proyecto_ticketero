@echo off
echo ========================================
echo  CONFIGURANDO ENTORNO DE TESTING
echo ========================================

echo.
echo [1/3] Instalando dependencias de testing...
call npm install @testing-library/svelte @testing-library/jest-dom @testing-library/user-event @playwright/test jsdom --save-dev
if %errorlevel% neq 0 (
    echo ERROR: Fallo al instalar dependencias
    pause
    exit /b 1
)

echo.
echo [2/3] Instalando navegadores para Playwright...
call npx playwright install
if %errorlevel% neq 0 (
    echo ERROR: Fallo al instalar navegadores
    pause
    exit /b 1
)

echo.
echo [3/3] Verificando configuracion...
call npm run check
if %errorlevel% neq 0 (
    echo ERROR: Verificacion de TypeScript fallo
    pause
    exit /b 1
)

echo.
echo ========================================
echo  CONFIGURACION DE TESTING COMPLETADA
echo ========================================
echo.
echo Comandos disponibles:
echo - npm run test:unit        (Tests unitarios)
echo - npm run test:e2e         (Tests E2E)
echo - npm run test:all         (Todos los tests)
echo - scripts\run-tests.bat    (Suite completa)
echo.
pause