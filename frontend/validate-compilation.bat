@echo off
echo ========================================
echo Validando Compilación - FASE 1
echo ========================================

echo.
echo 1. Verificando Node.js...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Node.js no está instalado
    echo 📥 Instalar desde: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

echo ✅ Node.js disponible

echo.
echo 2. Instalando dependencias...
npm install
if %errorlevel% neq 0 (
    echo ❌ ERROR: Falló la instalación de dependencias
    pause
    exit /b 1
)

echo ✅ Dependencias instaladas

echo.
echo 3. Verificando TypeScript (svelte-check)...
npm run check
if %errorlevel% neq 0 (
    echo ❌ ERROR: Falló la verificación de TypeScript
    pause
    exit /b 1
)

echo ✅ TypeScript verificado

echo.
echo 4. Compilando proyecto...
npm run build
if %errorlevel% neq 0 (
    echo ❌ ERROR: Falló la compilación
    pause
    exit /b 1
)

echo ✅ Compilación exitosa

echo.
echo 5. Verificando linting...
npm run lint
if %errorlevel% neq 0 (
    echo ⚠️  WARNING: Hay problemas de linting (no crítico)
) else (
    echo ✅ Linting correcto
)

echo.
echo ========================================
echo ✅ FASE 1 - VALIDACIÓN COMPLETADA
echo ========================================
echo.
echo Archivos validados:
echo - src/lib/types/index.ts
echo - src/lib/utils/constants.ts  
echo - src/lib/utils/helpers.ts
echo - src/lib/utils/env.ts
echo - src/lib/index.ts
echo - src/routes/+page.svelte
echo.
echo Para iniciar desarrollo:
echo npm run dev
echo.
pause