# Guía de Pruebas Locales - Sistema Ticketero

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Propósito:** Validación preliminar completa del sistema

---

## ÍNDICE

1. [Configuración Inicial](#1-configuración-inicial)
2. [Casos de Prueba Principales](#2-casos-de-prueba-principales)
3. [Pruebas por Requerimiento Funcional](#3-pruebas-por-requerimiento-funcional)
4. [Pruebas de Integración](#4-pruebas-de-integración)
5. [Validación de Datos](#5-validación-de-datos)
6. [Checklist de Validación](#6-checklist-de-validación)

---

## 1. Configuración Inicial

### 1.1 Prerrequisitos

**Software requerido:**
- Java 21 instalado
- PostgreSQL 16 corriendo en puerto 5432
- Postman o curl para pruebas de API
- Navegador web para dashboard

**Base de datos:**
```sql
-- Crear base de datos
CREATE DATABASE ticketero_db;

-- Verificar conexión
\c ticketero_db
\dt
```

### 1.2 Iniciar el Sistema

```bash
# 1. Clonar y navegar al proyecto
cd proyecto_ticketero

# 2. Iniciar aplicación
./mvnw spring-boot:run

# 3. Verificar que está corriendo
curl http://localhost:8080/api/health
```

**Respuesta esperada:**
```json
{
  "status": "UP",
  "timestamp": "2025-01-XX..."
}
```

### 1.3 Datos de Prueba Iniciales

El sistema se inicia con 5 asesores predefinidos:

| ID | Nombre | Módulo | Estado |
|----|--------|--------|--------|
| 1 | Ana García | 1 | AVAILABLE |
| 2 | Carlos López | 2 | AVAILABLE |
| 3 | María Rodríguez | 3 | AVAILABLE |
| 4 | Juan Pérez | 4 | AVAILABLE |
| 5 | Laura Martínez | 5 | AVAILABLE |

---

## 2. Casos de Prueba Principales

### CP-001: Flujo Completo Happy Path

**Objetivo:** Validar el flujo completo desde creación hasta completado

**Pasos:**

1. **Crear ticket**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```

**Validar respuesta:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "ticketNumber": "C01",
  "queueType": "CAJA",
  "status": "EN_ESPERA",
  "position": 1,
  "estimatedWaitTime": 5,
  "createdAt": "2025-01-XX..."
}
```

2. **Verificar posición**
```bash
curl http://localhost:8080/api/tickets/C01/position
```

3. **Consultar dashboard**
```bash
curl http://localhost:8080/api/admin/dashboard
```

4. **Simular asignación automática** (esperar 30 segundos para scheduler)

5. **Verificar estado actualizado**
```bash
curl http://localhost:8080/api/tickets/C01/position
```

**Resultado esperado:** Ticket pasa de EN_ESPERA → ATENDIENDO

---

### CP-002: Múltiples Tickets en Diferentes Colas

**Objetivo:** Validar gestión de múltiples colas y prioridades

**Pasos:**

1. **Crear ticket CAJA (prioridad 1)**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "11111111-1",
    "phoneNumber": "+56911111111",
    "queueType": "CAJA"
  }'
```

2. **Crear ticket GERENCIA (prioridad 4)**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "22222222-2",
    "phoneNumber": "+56922222222",
    "queueType": "GERENCIA"
  }'
```

3. **Crear ticket PERSONAL_BANKER (prioridad 2)**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "33333333-3",
    "phoneNumber": "+56933333333",
    "queueType": "PERSONAL_BANKER"
  }'
```

4. **Verificar orden de asignación**
```bash
curl http://localhost:8080/api/admin/dashboard
```

**Resultado esperado:** GERENCIA se asigna primero (mayor prioridad)

---

### CP-003: Validación de Reglas de Negocio

**Objetivo:** Validar que las reglas de negocio se cumplan

**Pasos:**

1. **Crear primer ticket**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```

2. **Intentar crear segundo ticket con mismo RUT**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "PERSONAL_BANKER"
  }'
```

**Resultado esperado:** Error 409 Conflict
```json
{
  "message": "Cliente ya tiene un ticket activo",
  "status": 409,
  "timestamp": "2025-01-XX..."
}
```

---

## 3. Pruebas por Requerimiento Funcional

### RF-001: Crear Ticket Digital

**Casos de prueba:**

**TC-001.1: Creación exitosa**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 201, UUID generado, número C01

**TC-001.2: RUT inválido**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "123",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 400, mensaje de error

**TC-001.3: Teléfono inválido**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "123",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 400, mensaje de error

**TC-001.4: Ticket duplicado**
```bash
# Crear primer ticket
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'

# Intentar crear segundo
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "PERSONAL_BANKER"
  }'
```
✅ **Validar:** Status 409, mensaje "Cliente ya tiene un ticket activo"

---

### RF-002: Enviar Notificaciones Automáticas

**Casos de prueba:**

**TC-002.1: Mensaje de confirmación**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** 
- Revisar logs: "Mensaje enviado exitosamente"
- Verificar en base de datos tabla `mensaje`

**TC-002.2: Verificar plantilla de mensaje**
```sql
-- Ejecutar en PostgreSQL
SELECT * FROM mensaje WHERE template = 'totem_ticket_creado' ORDER BY created_at DESC LIMIT 1;
```
✅ **Validar:** Mensaje contiene número de ticket, posición y tiempo estimado

---

### RF-003: Calcular Posición y Tiempo Estimado

**Casos de prueba:**

**TC-003.1: Cálculo de posición**
```bash
# Crear 3 tickets CAJA
for i in {1..3}; do
  curl -X POST http://localhost:8080/api/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"nationalId\": \"1111111$i-1\",
      \"phoneNumber\": \"+5691111111$i\",
      \"queueType\": \"CAJA\"
    }"
done

# Consultar posición del último
curl http://localhost:8080/api/tickets/C03/position
```
✅ **Validar:** position = 3, estimatedWaitTime = 15 (3 × 5 min)

**TC-003.2: Recálculo automático**
```bash
# Después de que se asigne un ticket, verificar recálculo
curl http://localhost:8080/api/tickets/C02/position
```
✅ **Validar:** position = 1, estimatedWaitTime = 5

---

### RF-004: Asignar Ticket a Ejecutivo Automáticamente

**Casos de prueba:**

**TC-004.1: Asignación automática**
```bash
# Crear ticket
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'

# Esperar 30 segundos (scheduler)
sleep 30

# Verificar asignación
curl http://localhost:8080/api/tickets/C01/position
```
✅ **Validar:** status = "ATENDIENDO", advisorName presente

**TC-004.2: Balanceo de carga**
```bash
# Crear múltiples tickets
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"nationalId\": \"1111111$i-1\",
      \"phoneNumber\": \"+5691111111$i\",
      \"queueType\": \"CAJA\"
    }"
done

# Verificar distribución
curl http://localhost:8080/api/admin/advisors/stats
```
✅ **Validar:** Tickets distribuidos equitativamente entre asesores

---

### RF-005: Gestionar Múltiples Colas

**Casos de prueba:**

**TC-005.1: Estadísticas por cola**
```bash
curl http://localhost:8080/api/admin/queues/CAJA/stats
```
✅ **Validar:** waiting, total, averageWaitTime

**TC-005.2: Estado de cola específica**
```bash
curl http://localhost:8080/api/admin/queues/GERENCIA
```
✅ **Validar:** Lista de tickets en cola GERENCIA

---

### RF-006: Consultar Estado del Ticket

**Casos de prueba:**

**TC-006.1: Consulta por UUID**
```bash
# Obtener UUID del ticket creado
UUID="550e8400-e29b-41d4-a716-446655440000"
curl http://localhost:8080/api/tickets/$UUID
```
✅ **Validar:** Información completa del ticket

**TC-006.2: Consulta por número**
```bash
curl http://localhost:8080/api/tickets/C01/position
```
✅ **Validar:** Posición y tiempo estimado actualizados

**TC-006.3: Ticket inexistente**
```bash
curl http://localhost:8080/api/tickets/X99/position
```
✅ **Validar:** Status 404, mensaje "Ticket no encontrado"

---

### RF-007: Panel de Monitoreo para Supervisor

**Casos de prueba:**

**TC-007.1: Dashboard completo**
```bash
curl http://localhost:8080/api/admin/dashboard
```
✅ **Validar:** 
- totalTickets, waitingTickets, attendingTickets
- queueStats para cada tipo de cola
- advisorStats con estado de cada asesor

**TC-007.2: Resumen simplificado**
```bash
curl http://localhost:8080/api/admin/summary
```
✅ **Validar:** Métricas principales resumidas

**TC-007.3: Estado de asesores**
```bash
curl http://localhost:8080/api/admin/advisors
```
✅ **Validar:** Lista completa de asesores con estado

**TC-007.4: Cambiar estado de asesor**
```bash
curl -X PUT http://localhost:8080/api/admin/advisors/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "OFFLINE"}'
```
✅ **Validar:** Status 200, asesor cambia a OFFLINE

---

### RF-008: Registrar Auditoría de Eventos

**Casos de prueba:**

**TC-008.1: Verificar auditoría de creación**
```sql
-- Ejecutar en PostgreSQL después de crear ticket
SELECT * FROM audit_log WHERE event_type = 'TICKET_CREATED' ORDER BY timestamp DESC LIMIT 1;
```
✅ **Validar:** Registro con timestamp, actor, entityId

**TC-008.2: Verificar auditoría de asignación**
```sql
SELECT * FROM audit_log WHERE event_type = 'TICKET_ASSIGNED' ORDER BY timestamp DESC LIMIT 1;
```
✅ **Validar:** Registro con información del asesor asignado

---

## 4. Pruebas de Integración

### PI-001: Flujo Completo con Múltiples Actores

**Escenario:** 3 clientes, 2 asesores disponibles, diferentes colas

**Pasos:**

1. **Configurar asesores**
```bash
# Poner 3 asesores OFFLINE
curl -X PUT http://localhost:8080/api/admin/advisors/3/status -H "Content-Type: application/json" -d '{"status": "OFFLINE"}'
curl -X PUT http://localhost:8080/api/admin/advisors/4/status -H "Content-Type: application/json" -d '{"status": "OFFLINE"}'
curl -X PUT http://localhost:8080/api/admin/advisors/5/status -H "Content-Type: application/json" -d '{"status": "OFFLINE"}'
```

2. **Crear tickets con diferentes prioridades**
```bash
# Cliente 1 - CAJA (prioridad 1)
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "11111111-1",
    "phoneNumber": "+56911111111",
    "queueType": "CAJA"
  }'

# Cliente 2 - GERENCIA (prioridad 4)
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "22222222-2",
    "phoneNumber": "+56922222222",
    "queueType": "GERENCIA"
  }'

# Cliente 3 - PERSONAL_BANKER (prioridad 2)
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "33333333-3",
    "phoneNumber": "+56933333333",
    "queueType": "PERSONAL_BANKER"
  }'
```

3. **Esperar asignaciones automáticas**
```bash
sleep 60
```

4. **Verificar orden de asignación**
```bash
curl http://localhost:8080/api/admin/dashboard
```

✅ **Validar:** 
- GERENCIA (G01) se asigna primero
- PERSONAL_BANKER (P01) se asigna segundo
- CAJA (C01) queda en espera

---

### PI-002: Prueba de Carga Básica

**Objetivo:** Validar comportamiento con múltiples tickets simultáneos

**Pasos:**

1. **Crear 10 tickets rápidamente**
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"nationalId\": \"1234567$i-$(($i % 10))\",
      \"phoneNumber\": \"+56912345$i\",
      \"queueType\": \"CAJA\"
    }" &
done
wait
```

2. **Verificar que todos se crearon**
```bash
curl http://localhost:8080/api/admin/queues/CAJA/stats
```

✅ **Validar:** 10 tickets creados, numeración secuencial correcta

---

## 5. Validación de Datos

### VD-001: Formatos de Entrada

**RUT Chileno válido:**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 201

**RUT sin guión:**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "123456789",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 201

**ID extranjero:**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "P12345678",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 201

**Teléfono nacional:**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 201, teléfono normalizado a +56912345678

---

### VD-002: Validaciones de Negocio

**Campos requeridos:**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+56912345678",
    "queueType": "CAJA"
  }'
```
✅ **Validar:** Status 400, "nationalId is required"

**Tipo de cola inválido:**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "INVALID"
  }'
```
✅ **Validar:** Status 400, mensaje de error

---

## 6. Checklist de Validación

### ✅ Funcionalidades Básicas

- [ ] Sistema inicia correctamente
- [ ] Health check responde OK
- [ ] Base de datos conecta
- [ ] Migraciones Flyway ejecutadas
- [ ] Asesores iniciales creados

### ✅ RF-001: Crear Ticket Digital

- [ ] Creación exitosa con datos válidos
- [ ] Validación de RUT/ID
- [ ] Validación de teléfono
- [ ] Prevención de tickets duplicados
- [ ] Numeración secuencial correcta
- [ ] Cálculo inicial de posición y tiempo

### ✅ RF-002: Notificaciones Telegram

- [ ] Mensaje de confirmación se envía
- [ ] Plantilla correcta aplicada
- [ ] Registro en tabla mensaje
- [ ] Manejo de errores de envío

### ✅ RF-003: Calcular Posición y Tiempo

- [ ] Cálculo inicial correcto
- [ ] Recálculo automático
- [ ] Fórmula de tiempo estimado
- [ ] Consulta por número de ticket

### ✅ RF-004: Asignar Ticket a Ejecutivo

- [ ] Asignación automática funciona
- [ ] Respeta prioridades de cola
- [ ] Balanceo de carga entre asesores
- [ ] Orden FIFO dentro de cola
- [ ] Cambio de estado a ATENDIENDO

### ✅ RF-005: Gestionar Múltiples Colas

- [ ] Estadísticas por cola
- [ ] Estado de cola específica
- [ ] Prioridades respetadas
- [ ] Prefijos correctos (C, P, E, G)

### ✅ RF-006: Consultar Estado del Ticket

- [ ] Consulta por UUID
- [ ] Consulta por número
- [ ] Información actualizada
- [ ] Manejo de tickets inexistentes

### ✅ RF-007: Panel de Monitoreo

- [ ] Dashboard completo
- [ ] Resumen simplificado
- [ ] Estado de asesores
- [ ] Cambio de estado de asesor
- [ ] Estadísticas de asesores
- [ ] Actualización en tiempo real

### ✅ RF-008: Auditoría de Eventos

- [ ] Registro de creación de tickets
- [ ] Registro de asignaciones
- [ ] Registro de cambios de estado
- [ ] Timestamps correctos
- [ ] Información completa en logs

### ✅ Reglas de Negocio

- [ ] RN-001: Un ticket activo por cliente
- [ ] RN-002: Prioridades de cola respetadas
- [ ] RN-003: Orden FIFO dentro de cola
- [ ] RN-004: Balanceo de carga
- [ ] RN-005: Formato de número de ticket
- [ ] RN-006: Prefijos por tipo de cola
- [ ] RN-010: Cálculo de tiempo estimado
- [ ] RN-013: Estados de asesor

### ✅ Validaciones y Errores

- [ ] Validación de entrada
- [ ] Mensajes de error claros
- [ ] Status HTTP correctos
- [ ] Manejo de excepciones
- [ ] Logs de errores

### ✅ Performance y Estabilidad

- [ ] Respuesta < 3 segundos para creación
- [ ] Dashboard actualiza cada 5 segundos
- [ ] Sin memory leaks evidentes
- [ ] Manejo de múltiples requests simultáneos

---

## Comandos Útiles para Debugging

**Ver logs en tiempo real:**
```bash
tail -f logs/application.log
```

**Consultar base de datos:**
```sql
-- Estado de tickets
SELECT ticket_number, status, queue_type, created_at FROM ticket ORDER BY created_at DESC;

-- Mensajes enviados
SELECT template, status, created_at FROM mensaje ORDER BY created_at DESC;

-- Asesores y su estado
SELECT name, status, assigned_tickets_count FROM advisor;
```

**Reiniciar numeración diaria:**
```sql
-- Simular nuevo día (resetea contadores)
UPDATE ticket SET created_at = CURRENT_DATE WHERE created_at < CURRENT_DATE;
```

---

**Preparado por:** Equipo QA  
**Versión:** 1.0  
**Última actualización:** Diciembre 2025

Esta guía proporciona una validación completa del sistema antes del despliegue en producción.