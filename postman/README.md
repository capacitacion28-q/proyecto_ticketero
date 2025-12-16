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
| `ticketNumber` | `G01` | Número de ticket para pruebas |
| `nationalId` | `12345678` | Cédula para pruebas |
| `advisorId` | `1` | ID de asesor para pruebas |
| `ticketId` | `1` | ID de ticket para pruebas admin |

## 🎯 Flujo de Pruebas Recomendado

### 1. **Verificar Salud**
```
GET /actuator/health
```

### 2. **Crear Ticket**
```
POST /api/tickets
{
  "nationalId": "12345678",
  "phoneNumber": "987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "GENERAL"
}
```

### 3. **Consultar Ticket Creado**
```
GET /api/tickets/{{ticketUuid}}
```

### 4. **Ver Dashboard**
```
GET /api/admin/dashboard
```

### 5. **Consultar Estado de Cola**
```
GET /api/admin/queue/GENERAL
```

### 6. **Gestionar Ticket (Admin)**
```
PUT /api/admin/tickets/1/status?status=ATENDIENDO
```

## 📝 Tipos de Cola Disponibles

- `GENERAL` - Cola general (prefijo G)
- `PRIORITY` - Cola prioritaria (prefijo P)
- `VIP` - Cola VIP (prefijo V)

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

## 🔄 Auto-Variables

La colección incluye un script que automáticamente guarda el UUID del ticket creado en la variable `ticketUuid` para usar en otras peticiones.

## 🐛 Troubleshooting

1. **Error de conexión**: Verificar que la aplicación esté corriendo en puerto 8081
2. **404 Not Found**: Verificar que el endpoint existe y está bien escrito
3. **500 Internal Error**: Revisar logs de la aplicación para detalles del error
4. **Variables no funcionan**: Asegurar que el environment "Ticketero - Local" esté seleccionado