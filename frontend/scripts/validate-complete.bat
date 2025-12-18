@echo off
echo ========================================
echo  VALIDACION COMPLETA DEL SISTEMA
echo ========================================

echo.
echo [1/6] Verificando estructura del proyecto...
if not exist "src\lib\components" (
    echo ERROR: Estructura de componentes no encontrada
    pause
    exit /b 1
)
if not exist "tests\unit" (
    echo ERROR: Tests unitarios no encontrados
    pause
    exit /b 1
)
if not exist "tests\e2e" (
    echo ERROR: Tests E2E no encontrados
    pause
    exit /b 1
)
echo ✅ Estructura del proyecto OK

echo.
echo [2/6] Verificando dependencias...
call npm list @testing-library/svelte @playwright/test vitest >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Dependencias faltantes
    echo Instalando dependencias...
    call npm install
)
echo ✅ Dependencias OK

echo.
echo [3/6] Verificando configuracion TypeScript...
call npm run check >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Errores de TypeScript encontrados
    call npm run check
    pause
    exit /b 1
)
echo ✅ TypeScript OK

echo.
echo [4/6] Ejecutando tests unitarios...
call npm run test:unit:coverage
if %errorlevel% neq 0 (
    echo ERROR: Tests unitarios fallaron
    pause
    exit /b 1
)
echo ✅ Tests unitarios OK

echo.
echo [5/6] Compilando para produccion...
call npm run build >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Build de produccion fallo
    call npm run build
    pause
    exit /b 1
)
echo ✅ Build OK

echo.
echo [6/6] Ejecutando tests E2E...
call npm run test:e2e
if %errorlevel% neq 0 (
    echo ERROR: Tests E2E fallaron
    pause
    exit /b 1
)
echo ✅ Tests E2E OK

echo.
echo ========================================
echo  ✅ VALIDACION COMPLETA EXITOSA
echo ========================================
echo.
echo Resumen:
echo - Tests unitarios: PASARON
echo - Tests E2E: PASARON  
echo - Build produccion: EXITOSO
echo - TypeScript: SIN ERRORES
echo.
echo Reportes generados:
echo - Coverage: coverage\index.html
echo - E2E: playwright-report\index.html
echo.
echo 🎉 EL SISTEMA ESTA LISTO PARA PRODUCCION
echo.
pause