@echo off
echo ========================================
echo Instalando NVM para Windows + Node.js
echo ========================================

echo.
echo PASO 1: Descargar NVM para Windows
echo.
echo Opciones de instalación:
echo.
echo OPCIÓN A - Descarga Manual (Recomendado):
echo 1. Ir a: https://github.com/coreybutler/nvm-windows/releases
echo 2. Descargar: nvm-setup.exe (latest release)
echo 3. Ejecutar como Administrador
echo 4. Seguir el wizard de instalación
echo.
echo OPCIÓN B - PowerShell (Requiere permisos):
echo Ejecutar PowerShell como Administrador y correr:
echo Invoke-WebRequest -Uri "https://github.com/coreybutler/nvm-windows/releases/download/1.1.11/nvm-setup.exe" -OutFile "nvm-setup.exe"
echo .\nvm-setup.exe
echo.

pause

echo.
echo PASO 2: Después de instalar NVM, reiniciar terminal y ejecutar:
echo.
echo # Verificar instalación
echo nvm version
echo.
echo # Instalar Node.js LTS
echo nvm install lts
echo.
echo # Usar la versión instalada
echo nvm use lts
echo.
echo # Verificar Node.js
echo node --version
echo npm --version
echo.

pause

echo.
echo PASO 3: Una vez instalado Node.js, ejecutar:
echo.
echo cd frontend
echo npm install
echo npm run check
echo npm run build
echo.

pause

echo.
echo ========================================
echo Instrucciones completas mostradas
echo ========================================
echo.
echo Próximos pasos:
echo 1. Instalar NVM desde GitHub releases
echo 2. Reiniciar terminal
echo 3. Instalar Node.js LTS con NVM
echo 4. Validar instalación del frontend
echo.