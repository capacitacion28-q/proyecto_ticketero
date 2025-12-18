# Guía de Instalación: Node.js con NVM

## 📥 Instalador Descargado

✅ **nvm-setup.exe** ya está descargado en esta carpeta (5.7 MB)

## 🚀 Pasos de Instalación

### PASO 1: Instalar NVM

1. **Ejecutar como Administrador**:
   - Click derecho en `nvm-setup.exe`
   - Seleccionar "Ejecutar como administrador"

2. **Seguir el wizard**:
   - ✅ Aceptar licencia
   - ✅ Ruta de instalación: `C:\Users\Usuario\AppData\Roaming\nvm`
   - ✅ Symlink de Node.js: `C:\Program Files\nodejs`
   - ✅ Completar instalación

3. **Reiniciar terminal** (IMPORTANTE)

### PASO 2: Verificar NVM

Abrir nueva terminal y ejecutar:
```bash
nvm version
```

Debería mostrar: `1.1.11` (o superior)

### PASO 3: Instalar Node.js LTS

```bash
# Instalar última versión LTS
nvm install lts

# Activar la versión instalada
nvm use lts

# Verificar instalación
node --version
npm --version
```

### PASO 4: Configurar Proyecto Frontend

**Opción A - Script Automático (Recomendado)**:
```bash
setup-node-with-nvm.bat
```

Este script hará automáticamente:
- ✅ Verificar NVM
- ✅ Instalar Node.js LTS
- ✅ Instalar dependencias (npm install)
- ✅ Verificar TypeScript (npm run check)
- ✅ Compilar proyecto (npm run build)

**Opción B - Manual**:
```bash
# Instalar dependencias
npm install

# Verificar TypeScript
npm run check

# Compilar proyecto
npm run build

# Iniciar desarrollo
npm run dev
```

## 🎯 Comandos NVM Útiles

```bash
# Ver versiones instaladas
nvm list

# Ver versiones disponibles
nvm list available

# Instalar versión específica
nvm install 18.19.0

# Usar versión específica
nvm use 18.19.0

# Desinstalar versión
nvm uninstall 18.19.0

# Ver versión actual
nvm current
```

## ✅ Verificación Final

Después de la instalación, verificar:

```bash
# 1. NVM instalado
nvm version
# Esperado: 1.1.11

# 2. Node.js instalado
node --version
# Esperado: v18.x.x o v20.x.x

# 3. npm instalado
npm --version
# Esperado: 9.x.x o 10.x.x

# 4. Proyecto compila
cd frontend
npm run build
# Esperado: Build exitoso sin errores
```

## 🐛 Solución de Problemas

### Problema: "nvm no se reconoce como comando"
**Solución**: 
- Reiniciar terminal completamente
- Verificar que NVM está en PATH: `echo %PATH%`
- Reinstalar NVM como Administrador

### Problema: "npm install falla"
**Solución**:
- Verificar conexión a internet
- Limpiar caché: `npm cache clean --force`
- Intentar nuevamente: `npm install`

### Problema: "npm run build falla"
**Solución**:
- Verificar que todas las dependencias están instaladas
- Ejecutar: `npm run check` para ver errores TypeScript
- Revisar logs en consola

## 📋 Checklist de Instalación

- [ ] nvm-setup.exe ejecutado como Administrador
- [ ] Terminal reiniciada
- [ ] `nvm version` funciona
- [ ] Node.js LTS instalado con `nvm install lts`
- [ ] Node.js activado con `nvm use lts`
- [ ] `node --version` muestra versión
- [ ] `npm --version` muestra versión
- [ ] `npm install` completado sin errores
- [ ] `npm run check` sin errores TypeScript
- [ ] `npm run build` compilación exitosa
- [ ] Listo para continuar con FASE 2

## 🎉 Próximos Pasos

Una vez completada la instalación:

1. ✅ Ejecutar `npm run dev` para iniciar desarrollo
2. ✅ Abrir http://localhost:5173 en navegador
3. ✅ Verificar que la página muestra "FASE 1 Completada"
4. ✅ Continuar con FASE 2: Services y API Client

---

**Instalador**: nvm-setup.exe (ya descargado)  
**Script automático**: setup-node-with-nvm.bat  
**Documentación NVM**: https://github.com/coreybutler/nvm-windows