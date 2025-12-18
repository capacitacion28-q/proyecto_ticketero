# ✅ Solución: Errores de Conexión Frontend → API

## 🔍 Problemas Identificados

1. **Puerto incorrecto:** Frontend configurado para `8080`, pero API corre en `8081`
2. **CORS no configurado:** Backend no permitía peticiones desde `localhost:5173`
3. **Variables de entorno no usadas:** Frontend usaba URLs hardcodeadas

---

## 🛠️ Cambios Aplicados

### 1. Configuración CORS en Backend

**Archivo creado:** `src/main/java/com/example/ticketero/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### 2. Corrección de Puerto en Frontend

**Archivo modificado:** `frontend/.env`

```env
# ANTES
VITE_API_URL=http://localhost:8080

# DESPUÉS
VITE_API_URL=http://localhost:8081
```

### 3. Uso de Variables de Entorno

**Archivo modificado:** `frontend/src/lib/utils/constants.ts`

```typescript
// ANTES
export const API_BASE_URL = 'http://localhost:8080/api';

// DESPUÉS
import { ENV } from './env';
export const API_BASE_URL = `${ENV.API_URL}/api`;
```

---

## 🚀 Cómo Aplicar los Cambios

### Opción 1: Reiniciar Todo (Recomendado)

```bash
# 1. Detener todo
stop-local.bat

# 2. Recompilar API (para incluir CorsConfig)
mvn clean package -DskipTests

# 3. Iniciar todo de nuevo
start-local.bat
```

### Opción 2: Solo Reiniciar API

```bash
# 1. Detener API (Ctrl+C en ventana de API)

# 2. Recompilar
mvn clean package -DskipTests

# 3. Reiniciar
mvn spring-boot:run
```

### Opción 3: Solo Reiniciar Frontend

```bash
# 1. Detener Frontend (Ctrl+C en ventana de Frontend)

# 2. Limpiar cache y reiniciar
cd frontend
npm run dev
```

---

## ✅ Verificación

### 1. Verificar API

```bash
# Health check
curl http://localhost:8081/actuator/health

# Crear ticket de prueba
curl -X POST http://localhost:8081/api/tickets \
  -H "Content-Type: application/json" \
  -d "{\"nationalId\":\"12345678-9\",\"phoneNumber\":\"+56912345678\",\"branchOffice\":\"Sucursal Centro\",\"queueType\":\"GERENCIA\"}"
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "codigoReferencia": "uuid-aqui",
  "numero": "G01",
  "status": "EN_ESPERA",
  ...
}
```

### 2. Verificar Frontend

1. Abrir: http://localhost:5173/totem
2. Llenar formulario:
   - RUT: 12345678-9
   - Teléfono: +56912345678
   - Sucursal: Sucursal Centro
   - Cola: GERENCIA
3. Click "Crear Ticket"
4. Debe mostrar ticket creado sin errores

### 3. Verificar CORS

Abrir DevTools (F12) → Console:
- ✅ No debe haber errores de CORS
- ✅ Debe ver: `🚀 API Request: POST /api/tickets`
- ✅ Debe ver: `✅ API Response: 200 /api/tickets`

---

## 🐛 Solución de Problemas

### Error: "CORS policy blocked"

**Causa:** API no reiniciada después de agregar CorsConfig

**Solución:**
```bash
# Recompilar y reiniciar API
mvn clean package -DskipTests
mvn spring-boot:run
```

### Error: "Network Error" o "ERR_CONNECTION_REFUSED"

**Causa:** API no está corriendo o puerto incorrecto

**Verificar:**
```bash
# Ver si API está corriendo
curl http://localhost:8081/actuator/health

# Ver procesos Java
tasklist | findstr java
```

**Solución:**
```bash
# Reiniciar API
mvn spring-boot:run
```

### Error: "Cannot read properties of undefined"

**Causa:** Frontend no recargó variables de entorno

**Solución:**
```bash
# Detener frontend (Ctrl+C)
cd frontend
npm run dev
```

### Frontend muestra datos viejos

**Causa:** Cache del navegador

**Solución:**
1. Abrir DevTools (F12)
2. Click derecho en botón Reload
3. Seleccionar "Empty Cache and Hard Reload"

---

## 📊 Estructura de Request Correcta

### Crear Ticket

```json
{
  "nationalId": "12345678-9",
  "phoneNumber": "+56912345678",
  "branchOffice": "Sucursal Centro",
  "queueType": "GERENCIA"
}
```

**Campos obligatorios:**
- `nationalId`: RUT con formato XX-X
- `phoneNumber`: Teléfono con +56
- `branchOffice`: Nombre de sucursal
- `queueType`: CAJA | PERSONAL_BANKER | EMPRESAS | GERENCIA

### Consultar Posición

```bash
GET /api/tickets/{codigoReferencia}/position
```

### Dashboard Admin

```bash
GET /api/admin/dashboard
```

---

## 🎯 Checklist de Verificación

- [ ] API corriendo en puerto 8081
- [ ] Frontend corriendo en puerto 5173
- [ ] PostgreSQL corriendo en puerto 5432
- [ ] CorsConfig compilado en API
- [ ] Variables de entorno actualizadas en frontend
- [ ] No hay errores CORS en consola del navegador
- [ ] Puedo crear tickets desde frontend
- [ ] Dashboard admin muestra datos

---

## 📝 Notas Importantes

1. **Puerto de API:** Siempre es `8081` (configurado en `application.yml`)
2. **Puerto de Frontend:** Siempre es `5173` (default de Vite)
3. **CORS:** Solo permite `localhost:5173`, `localhost:3000` y `127.0.0.1:5173`
4. **Recompilación:** Cambios en backend Java requieren recompilar con Maven
5. **Hot Reload:** Frontend tiene hot reload automático, backend NO

---

**Estado:** ✅ Problemas resueltos
**Fecha:** 2025-12-17
