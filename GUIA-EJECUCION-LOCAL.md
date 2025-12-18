# 🚀 Guía de Ejecución Local - Proyecto Ticketero

## 📋 Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

1. **Docker Desktop** (para PostgreSQL)
   - Descargar: https://www.docker.com/products/docker-desktop
   - Verificar: `docker --version`

2. **Java 21** (para API Spring Boot)
   - Descargar: https://adoptium.net/
   - Verificar: `java -version`

3. **Maven** (para compilar API)
   - Descargar: https://maven.apache.org/download.cgi
   - Verificar: `mvn -version`

4. **Node.js 18+** (para Frontend)
   - Descargar: https://nodejs.org/
   - Verificar: `node -version`

---

## ⚡ Inicio Rápido (Automático)

### Opción 1: Script Automático

```bash
# Ejecutar todo el proyecto
start-local.bat
```

Este script:
- ✅ Inicia PostgreSQL en Docker
- ✅ Inicia API Spring Boot en puerto 8080
- ✅ Inicia Frontend Svelte en puerto 5173

### Detener todo:

```bash
stop-local.bat
```

---

## 🔧 Inicio Manual (Paso a Paso)

### 1. Iniciar Base de Datos PostgreSQL

```bash
# Iniciar contenedor
docker-compose -f docker-compose.dev.yml up -d postgres

# Verificar que está corriendo
docker ps
```

**Credenciales:**
- Host: `localhost:5432`
- Database: `ticketero`
- User: `dev`
- Password: `dev123`

### 2. Iniciar API Spring Boot

```bash
# Compilar y ejecutar
mvn spring-boot:run

# O ejecutar el JAR compilado
mvn clean package
java -jar target/ticketero-1.0.0.jar
```

**API disponible en:** http://localhost:8080

**Endpoints principales:**
- `POST /api/tickets` - Crear ticket
- `GET /api/tickets/{id}` - Consultar ticket
- `GET /api/tickets/{id}/position` - Posición en cola
- `GET /api/admin/dashboard` - Dashboard admin

### 3. Iniciar Frontend Svelte

```bash
# Ir a carpeta frontend
cd frontend

# Instalar dependencias (solo primera vez)
npm install

# Iniciar servidor de desarrollo
npm run dev
```

**Frontend disponible en:** http://localhost:5173

---

## 🌐 URLs del Sistema

| Componente | URL | Descripción |
|------------|-----|-------------|
| **Frontend Principal** | http://localhost:5173 | Página de inicio |
| **Totem Cliente** | http://localhost:5173/totem | Crear tickets |
| **Dashboard Admin** | http://localhost:5173/admin | Panel administrativo |
| **API REST** | http://localhost:8080 | Backend API |
| **API Docs** | http://localhost:8080/actuator | Actuator endpoints |

---

## 🧪 Verificar que Todo Funciona

### 1. Verificar Base de Datos

```bash
# Conectar a PostgreSQL
docker exec -it ticketero-postgres psql -U dev -d ticketero

# Ver tablas
\dt

# Salir
\q
```

### 2. Verificar API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Crear ticket de prueba
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d "{\"rut\":\"12345678-9\",\"nombre\":\"Juan\",\"apellido\":\"Perez\",\"telefono\":\"+56912345678\",\"queueType\":\"GERENCIA\"}"
```

### 3. Verificar Frontend

Abrir navegador en:
- http://localhost:5173/totem (crear ticket)
- http://localhost:5173/admin (ver dashboard)

---

## 🐛 Solución de Problemas

### Error: "Puerto 5432 ya está en uso"

```bash
# Detener PostgreSQL local si existe
net stop postgresql-x64-16

# O cambiar puerto en docker-compose.dev.yml
ports:
  - "5433:5432"  # Usar puerto 5433 en lugar de 5432
```

### Error: "Puerto 8080 ya está en uso"

```bash
# Encontrar proceso usando puerto 8080
netstat -ano | findstr :8080

# Matar proceso (reemplazar PID)
taskkill /PID <PID> /F

# O cambiar puerto en application.yml
server:
  port: 8081
```

### Error: "Puerto 5173 ya está en uso"

```bash
# Matar procesos Node
taskkill /f /im node.exe

# O cambiar puerto en vite.config.js
server: {
  port: 5174
}
```

### Error: "Cannot connect to database"

```bash
# Verificar que PostgreSQL está corriendo
docker ps

# Ver logs de PostgreSQL
docker logs ticketero-postgres

# Reiniciar contenedor
docker-compose -f docker-compose.dev.yml restart postgres
```

### Error: "Flyway migration failed"

```bash
# Limpiar base de datos y reiniciar
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d postgres
```

---

## 📦 Comandos Útiles

### Docker

```bash
# Ver contenedores corriendo
docker ps

# Ver logs de PostgreSQL
docker logs -f ticketero-postgres

# Detener todo
docker-compose -f docker-compose.dev.yml down

# Limpiar volúmenes (CUIDADO: borra datos)
docker-compose -f docker-compose.dev.yml down -v
```

### Maven (API)

```bash
# Compilar sin tests
mvn clean package -DskipTests

# Ejecutar tests
mvn test

# Limpiar target
mvn clean
```

### NPM (Frontend)

```bash
# Instalar dependencias
npm install

# Desarrollo
npm run dev

# Build producción
npm run build

# Preview build
npm run preview

# Linting
npm run lint
```

---

## 🔄 Flujo de Desarrollo

1. **Iniciar servicios:**
   ```bash
   start-local.bat
   ```

2. **Hacer cambios en código:**
   - Backend: Los cambios requieren reiniciar API
   - Frontend: Hot reload automático

3. **Probar cambios:**
   - Frontend: http://localhost:5173
   - API: http://localhost:8080

4. **Detener servicios:**
   ```bash
   stop-local.bat
   ```

---

## 📊 Monitoreo

### Logs de API

```bash
# Ver logs en tiempo real
tail -f logs/ticketero.log

# En Windows
type logs\ticketero.log
```

### Logs de Frontend

Los logs aparecen en la consola donde ejecutaste `npm run dev`

### Logs de PostgreSQL

```bash
docker logs -f ticketero-postgres
```

---

## 🎯 Casos de Uso Rápidos

### Crear un Ticket

1. Ir a http://localhost:5173/totem
2. Llenar formulario:
   - RUT: 12345678-9
   - Nombre: Juan
   - Apellido: Pérez
   - Teléfono: +56912345678
   - Cola: GERENCIA
3. Click "Crear Ticket"

### Ver Dashboard Admin

1. Ir a http://localhost:5173/admin
2. Ver estadísticas en tiempo real
3. Ver tickets por cola
4. Ver ejecutivos disponibles

### Consultar Posición

1. Usar el código del ticket creado
2. Hacer GET a: `http://localhost:8080/api/tickets/{codigo}/position`

---

## 📝 Notas Importantes

- ⚠️ **No usar en producción:** Esta configuración es solo para desarrollo local
- 🔒 **Credenciales:** Las credenciales son de desarrollo, cambiar en producción
- 💾 **Datos:** Los datos se persisten en volumen Docker `postgres_data`
- 🔄 **Hot Reload:** Frontend tiene hot reload, API requiere reinicio
- 📱 **Telegram:** Para notificaciones, configurar `TELEGRAM_BOT_TOKEN` en `.env`

---

## ✅ Checklist de Inicio

- [ ] Docker Desktop instalado y corriendo
- [ ] Java 21 instalado
- [ ] Maven instalado
- [ ] Node.js 18+ instalado
- [ ] Puerto 5432 disponible (PostgreSQL)
- [ ] Puerto 8080 disponible (API)
- [ ] Puerto 5173 disponible (Frontend)
- [ ] Ejecutar `start-local.bat`
- [ ] Verificar http://localhost:5173
- [ ] Verificar http://localhost:8080/actuator/health

---

**¡Listo para desarrollar! 🚀**
