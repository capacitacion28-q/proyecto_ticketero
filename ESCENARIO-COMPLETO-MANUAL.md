# 🎯 Escenario Completo: Flujo Manual de Atención de Ticket

## 📋 Descripción del Escenario
**Usuario solicita ticket → Espera → Ejecutivo se desocupa → Asignación → Atención → Completado**

---

## 🚀 Pasos para Replicar Manualmente

### **1. 🎫 Usuario pide un ticket**
```bash
curl -X POST http://localhost:8081/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678",
    "phoneNumber": "987654321",
    "branchOffice": "Sucursal Centro",
    "queueType": "CAJA"
  }'
```

**📝 Respuesta esperada:**
```json
{
  "id": 13,
  "codigoReferencia": "087ba617-e999-4cb2-8e69-b475ec907917",
  "numero": "C11",
  "status": "EN_ESPERA",
  "positionInQueue": 1,
  "estimatedWaitMinutes": 5,
  "telefono": "+56987654321"
}
```

**🔖 Guardar para siguientes pasos:**
- `id`: 13
- `numero`: C11  
- `codigoReferencia`: 087ba617-e999-4cb2-8e69-b475ec907917

---

### **2. ⏳ Verificar que está en espera**
```bash
curl -X GET http://localhost:8081/api/tickets/087ba617-e999-4cb2-8e69-b475ec907917
```

**📝 Respuesta esperada:**
```json
{
  "numero": "C11",
  "status": "ATENDIENDO",  // ⚡ Scheduler automático ya lo cambió
  "queueType": "CAJA",
  "positionInQueue": 1
}
```

---

### **3. 📊 Ver estado inicial del dashboard**
```bash
curl -X GET http://localhost:8081/api/admin/summary
```

**📝 Respuesta esperada:**
```json
{
  "totalTicketsToday": 3,
  "ticketsInQueue": 0,
  "ticketsBeingServed": 0,
  "ticketsCompleted": 0,
  "availableAdvisors": 5
}
```

---

### **4. 👨💼 Ejecutivo se desocupa**
```bash
curl -X PUT http://localhost:8081/api/admin/advisors/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "AVAILABLE"}'
```

**📝 Respuesta esperada:** `200 OK` (sin contenido)

---

### **5. 🔗 Asignar ticket al ejecutivo**
```bash
curl -X PUT http://localhost:8081/api/admin/tickets/13/assign/1
```

**📝 Respuesta esperada:** `200 OK` (sin contenido)

---

### **6. 🏃♂️ Verificar que está siendo atendido**
```bash
curl -X GET http://localhost:8081/api/tickets/C11/position
```

**📝 Respuesta esperada:**
```json
{
  "numero": "C11",
  "queueType": "CAJA",
  "status": "ATENDIENDO",
  "positionInQueue": 1,
  "assignedModuleNumber": 1,
  "message": "Ticket en cola"
}
```

---

### **7. ✅ Completar la atención**
```bash
curl -X PUT "http://localhost:8081/api/admin/tickets/13/status?status=COMPLETADO"
```

**📝 Respuesta esperada:** `200 OK` (sin contenido)

---

### **8. 🔍 Verificar estado final del ticket**
```bash
curl -X GET http://localhost:8081/api/tickets/C11/position
```

**📝 Respuesta esperada:**
```json
{
  "numero": "C11",
  "queueType": "CAJA",
  "status": "COMPLETADO",  // ✅ Estado final
  "assignedModuleNumber": 1
}
```

---

### **9. 📈 Dashboard final**
```bash
curl -X GET http://localhost:8081/api/admin/summary
```

**📝 Respuesta esperada:**
```json
{
  "totalTicketsToday": 3,
  "ticketsInQueue": 0,
  "ticketsBeingServed": 0,
  "ticketsCompleted": 1,  // ✅ Incrementado
  "availableAdvisors": 5
}
```

---

## 🎯 Resumen del Flujo Exitoso

| Paso | Acción | Estado Ticket | Observación |
|------|--------|---------------|-------------|
| 1 | Crear ticket | `EN_ESPERA` | Usuario solicita atención |
| 2 | Verificar | `ATENDIENDO` | Scheduler automático |
| 3 | Dashboard | - | Estado inicial del sistema |
| 4 | Asesor disponible | - | Ejecutivo se libera |
| 5 | Asignar | `ATENDIENDO` | Ticket → Asesor 1 |
| 6 | Verificar | `ATENDIENDO` | Módulo 1 asignado |
| 7 | Completar | `COMPLETADO` | Atención finalizada |
| 8 | Verificar final | `COMPLETADO` | Estado persistente |
| 9 | Dashboard final | - | Métricas actualizadas |

## ✅ Indicadores de Éxito

- ✅ **Ticket creado** con número único (C11)
- ✅ **Teléfono normalizado** (+56987654321)
- ✅ **Scheduler funcionando** (EN_ESPERA → ATENDIENDO automático)
- ✅ **Asignación exitosa** a módulo 1
- ✅ **Estado completado** correctamente
- ✅ **Dashboard actualizado** (ticketsCompleted: 1)

## 🔧 Variables para Postman

Después del paso 1, actualizar estas variables:
- `ticketUuid` = `087ba617-e999-4cb2-8e69-b475ec907917`
- `ticketNumber` = `C11`
- `ticketId` = `13`

**El escenario demuestra el ciclo completo de vida de un ticket funcionando correctamente.**