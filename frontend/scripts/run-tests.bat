@echo off
echo ========================================
echo  EJECUTANDO SUITE COMPLETA DE PRUEBAS
echo ========================================

echo.
echo [1/4] Verificando dependencias...
call npm list @testing-library/svelte @playwright/test vitest >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Dependencias de testing no instaladas
    echo Ejecutando: npm install
    call npm install
    if %errorlevel% neq 0 (
        echo ERROR: Fallo al instalar dependencias
        pause
        exit /b 1
    )
)

echo.
echo [2/4] Ejecutando tests unitarios con coverage...
call npm run test:unit:coverage
if %errorlevel% neq 0 (
    echo ERROR: Tests unitarios fallaron
    pause
    exit /b 1
)

echo.
echo [3/4] Compilando aplicacion para E2E...
call npm run build
if %errorlevel% neq 0 (
    echo ERROR: Build fallo
    pause
    exit /b 1
)

echo.
echo [4/4] Ejecutando tests E2E...
call npm run test:e2e
if %errorlevel% neq 0 (
    echo ERROR: Tests E2E fallaron
    pause
    exit /b 1
)

echo.
echo ========================================
echo  TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE
echo ========================================
echo.
echo Reportes generados:
echo - Unit tests: coverage/index.html
echo - E2E tests: playwright-report/index.html
echo.
pause