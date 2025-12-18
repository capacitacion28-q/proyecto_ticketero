@echo off
echo ========================================
echo Setup Node.js con NVM
echo ========================================

echo.
echo Verificando NVM...
nvm version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ NVM no está instalado o no está en el PATH
    echo.
    echo Por favor:
    echo 1. Ejecutar nvm-setup.exe como Administrador
    echo 2. Reiniciar esta terminal
    echo 3. Ejecutar este script nuevamente
    echo.
    pause
    exit /b 1
)

echo ✅ NVM instalado: 
nvm version

echo.
echo Instalando Node.js LTS...
nvm install lts
if %errorlevel% neq 0 (
    echo ❌ Error instalando Node.js
    pause
    exit /b 1
)

echo.
echo Activando Node.js LTS...
nvm use lts
if %errorlevel% neq 0 (
    echo ❌ Error activando Node.js
    pause
    exit /b 1
)

echo.
echo ✅ Node.js instalado:
node --version
npm --version

echo.
echo ========================================
echo Instalando dependencias del proyecto...
echo ========================================

npm install
if %errorlevel% neq 0 (
    echo ❌ Error instalando dependencias
    pause
    exit /b 1
)

echo.
echo ✅ Dependencias instaladas

echo.
echo ========================================
echo Validando compilación...
echo ========================================

echo.
echo Verificando TypeScript...
call npm run check
if %errorlevel% neq 0 (
    echo ❌ Error en verificación TypeScript
    pause
    exit /b 1
)

echo.
echo ✅ TypeScript OK

echo.
echo Compilando proyecto...
call npm run build
if %errorlevel% neq 0 (
    echo ❌ Error en compilación
    pause
    exit /b 1
)

echo.
echo ✅ Compilación exitosa

echo.
echo ========================================
echo ✅ SETUP COMPLETADO EXITOSAMENTE
echo ========================================
echo.
echo Node.js y proyecto configurados correctamente
echo.
echo Para iniciar desarrollo:
echo npm run dev
echo.
pause