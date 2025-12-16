# 📮 Colección Postman - Ticketero API

## 🚀 Importar en Postman

1. **Abrir Postman**
2. **Import** → **File** → Seleccionar `Ticketero-API.postman_collection.json`
3. **Import** → **File** → Seleccionar `Environment-Local.postman_environment.json`
4. **Seleccionar environment** "Ticketero - Local" en la esquina superior derecha

## 📁 Estructura de la Colección

### 🎫 **Tickets - Público**
Endpoints accesibles desde tótems y aplicaciones cliente:
- `POST /api/tickets` - Crear nuevo ticket
- `GET /api/tickets/{uuid}` - Consultar ticket por UUID
- `GET /api/tickets/{numero}/position` - Consultar posición por número
- `GET /api/tickets/by-national-id/{nationalId}` - Consultar tickets por cédula

### 📊 **Dashboard - Admin**
Endpoints administrativos para métricas:
- `GET /api/admin/dashboard` - Dashboard completo
- `GET /api/admin/summary` - Resumen simplificado
- `GET /api/admin/queue/{queueType}` - Estado de cola específica

### 👥 **Asesores - Admin**
Gestión administrativa de asesores:
- `GET /api/admin/advisors` - Lista de asesores
- `GET /api/admin/advisors/stats` - Estadísticas de asesores
- `PUT /api/admin/advisors/{id}/status` - Cambiar estado de asesor

### 🔧 **Gestión Tickets - Admin**
Operaciones administrativas sobre tickets:
- `PUT /api/admin/tickets/{id}/status` - Actualizar estado de ticket
- `PUT /api/admin/tickets/{ticketId}/assign/{advisorId}` - Asignar ticket a asesor

### 🏥 **Health Check**
Monitoreo y salud de la aplicación:
- `GET /actuator/health` - Estado de salud
- `GET /actuator/info` - Información de la aplicación
- `GET /actuator/metrics` - Métricas

## 🔧 Variables de Environment

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `baseUrl` | `http://localhost:8081` | URL base de la API |
| `ticketUuid` | Auto-generado | UUID del último ticket creado |
| `ticketNumber` | `C01` | Número de ticket para pruebas |
| `nationalId` | `12345678` | Cédula para pruebas |
| `advisorId` | `1` | ID de asesor para pruebas |
| `ticketId` | `1` | ID de ticket para pruebas admin |

## 🎯 Escenario Completo: Flujo de Atención de Ticket

### **📋 Escenario:** Usuario solicita ticket → Espera → Asignación → Atención → Completado

### **1. 🎫 Usuario pide un ticket**
```http
POST /api/tickets
Content-Type: application/json

{
  "nationalId": "12345678",
  "phoneNumber": "987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA"
}
```
**Respuesta esperada:**
```json
{
  "id": 13,
  "numero": "C11",
  "status": "EN_ESPERA",
  "positionInQueue": 1,
  "estimatedWaitMinutes": 5,
  "codigoReferencia": "087ba617-e999-4cb2-8e69-b475ec907917"
}
```
**📝 Guardar:** `numero` y `codigoReferencia` para siguientes pasos

### **2. ⏳ Verificar que está en espera**
```http
GET /api/tickets/{{codigoReferencia}}
```
**Respuesta esperada:**
```json
{
  "numero": "C11",
  "status": "ATENDIENDO",  // Scheduler automático
  "positionInQueue": 1
}
```

### **3. 📊 Ver estado inicial del dashboard**
```http
GET /api/admin/summary
```
**Respuesta esperada:**
```json
{
  "totalTicketsToday": 3,
  "ticketsInQueue": 0,
  "ticketsCompleted": 0,
  "availableAdvisors": 5
}
```

### **4. 👨💼 Ejecutivo se desocupa**
```http
PUT /api/admin/advisors/1/status
Content-Type: application/json

{
  "status": "AVAILABLE"
}
```
**Respuesta:** `200 OK`

### **5. 🔗 Asignar ticket al ejecutivo**
```http
PUT /api/admin/tickets/{{ticketId}}/assign/1
```
**Usar el `id` del ticket creado en paso 1**
**Respuesta:** `200 OK`

### **6. 🏃♂️ Verificar que está siendo atendido**
```http
GET /api/tickets/{{numero}}/position
```
**Respuesta esperada:**
```json
{
  "numero": "C11",
  "status": "ATENDIENDO",
  "assignedModuleNumber": 1,
  "message": "Ticket en cola"
}
```

### **7. ✅ Completar la atención**
```http
PUT /api/admin/tickets/{{ticketId}}/status?status=COMPLETADO
```
**Respuesta:** `200 OK`

### **8. 🔍 Verificar estado final**
```http
GET /api/tickets/{{numero}}/position
```
**Respuesta esperada:**
```json
{
  "numero": "C11",
  "status": "COMPLETADO",
  "assignedModuleNumber": 1
}
```

### **9. 📈 Dashboard final**
```http
GET /api/admin/summary
```
**Respuesta esperada:**
```json
{
  "totalTicketsToday": 3,
  "ticketsCompleted": 1,  // ✅ Incrementado
  "availableAdvisors": 5
}
```

## 🎯 Flujo Rápido de Verificación

### **Health Check**
```http
GET /actuator/health
```

### **Ver Dashboard Completo**
```http
GET /api/admin/dashboard
```

### **Consultar Estado de Colas**
```http
GET /api/admin/queue/CAJA
GET /api/admin/queue/PERSONAL_BANKER
GET /api/admin/queue/EMPRESAS
GET /api/admin/queue/GERENCIA
```

## 📝 Tipos de Cola Disponibles

- `CAJA` - Caja (prefijo C, 5 min promedio)
- `PERSONAL_BANKER` - Personal Banker (prefijo P, 15 min promedio)
- `EMPRESAS` - Empresas (prefijo E, 20 min promedio)
- `GERENCIA` - Gerencia (prefijo G, 30 min promedio)

## 📝 Estados de Ticket

- `EN_ESPERA` - En cola esperando
- `PROXIMO` - Próximo en ser atendido
- `ATENDIENDO` - Siendo atendido
- `COMPLETADO` - Atención completada
- `CANCELADO` - Ticket cancelado

## 📝 Estados de Asesor

- `AVAILABLE` - Disponible para atender
- `BUSY` - Ocupado atendiendo
- `BREAK` - En descanso
- `OFFLINE` - Fuera de línea

## 🔄 Variables para el Escenario

**Después del paso 1, actualizar estas variables manualmente:**
- `ticketUuid` = UUID del ticket creado
- `ticketNumber` = Número del ticket (ej: C11)
- `ticketId` = ID numérico del ticket (ej: 13)

**La colección incluye un script que automáticamente guarda el UUID del ticket creado.**

## 🐛 Troubleshooting

1. **Error de conexión**: Verificar que la aplicación esté corriendo en puerto 8081
2. **404 Not Found**: Verificar que el endpoint existe y está bien escrito
3. **500 Internal Error**: Revisar logs de la aplicación para detalles del error
4. **Variables no funcionan**: Asegurar que el environment "Ticketero - Local" esté seleccionado