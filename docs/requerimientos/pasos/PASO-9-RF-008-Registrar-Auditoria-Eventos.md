# PASO 9: RF-008 - Registrar Auditoría de Eventos

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Modelo de datos AuditLog con 7 campos
- ✅ Tipos de eventos auditables (8 eventos)
- ✅ Información capturada por evento
- ✅ 1 Regla de negocio aplicable
- ✅ 6 Escenarios Gherkin (diferentes eventos)

---

## RF-008: Registrar Auditoría de Eventos

**Descripción:**  
El sistema debe registrar automáticamente todos los eventos relevantes del ciclo de vida de tickets y operaciones del sistema, incluyendo: creación de tickets, asignaciones a ejecutivos, cambios de estado, envío de mensajes, cancelaciones, y acciones administrativas. Cada registro debe incluir: timestamp preciso, tipo de evento, actor involucrado (cliente, asesor, sistema), identificador de la entidad afectada, y cambios de estado (antes/después). Los registros de auditoría son inmutables y deben conservarse para análisis y cumplimiento normativo.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Sistema de auditoría operativo
- Base de datos accesible
- Evento auditable ocurre en el sistema

---

### Modelo de Datos (Entidad AuditLog)

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| id | BIGSERIAL | Primary key | 1 |
| timestamp | Timestamp | Fecha/hora exacta del evento | "2025-12-15T10:30:00.123Z" |
| tipoEvento | String | Tipo de evento auditable | "TICKET_CREADO" |
| actor | String | Quién ejecutó la acción | "cliente:12345678-9", "asesor:juan.perez", "sistema" |
| entityType | String | Tipo de entidad afectada | "Ticket", "Advisor", "Mensaje" |
| entityId | String | ID de la entidad afectada | "a1b2c3d4-...", "C05" |
| cambiosEstado | JSON | Estado antes/después | {"antes": "EN_ESPERA", "despues": "ATENDIENDO"} |
| metadata | JSON | Información adicional (opcional) | {"queueType": "CAJA", "moduleNumber": 3} |

**Total de campos:** 8

---

### Tipos de Eventos Auditables

| Evento | Descripción | Actor Típico | EntityType |
|--------|-------------|--------------|------------|
| TICKET_CREADO | Cliente crea nuevo ticket | cliente | Ticket |
| TICKET_ASIGNADO | Sistema asigna ticket a asesor | sistema | Ticket |
| TICKET_COMPLETADO | Asesor completa atención | asesor | Ticket |
| TICKET_CANCELADO | Cliente o sistema cancela ticket | cliente/sistema | Ticket |
| MENSAJE_ENVIADO | Sistema envía mensaje Telegram | sistema | Mensaje |
| MENSAJE_FALLIDO | Fallo en envío de mensaje | sistema | Mensaje |
| ASESOR_CAMBIO_ESTADO | Asesor cambia de estado | asesor/sistema | Advisor |
| CONSULTA_DASHBOARD | Supervisor consulta dashboard | supervisor | Dashboard |

---

### Información Capturada por Evento

#### TICKET_CREADO
```json
{
  "timestamp": "2025-12-15T10:30:00.123Z",
  "tipoEvento": "TICKET_CREADO",
  "actor": "cliente:12345678-9",
  "entityType": "Ticket",
  "entityId": "C05",
  "cambiosEstado": {
    "antes": null,
    "despues": "EN_ESPERA"
  },
  "metadata": {
    "queueType": "CAJA",
    "positionInQueue": 5,
    "estimatedWaitMinutes": 25
  }
}
```

#### TICKET_ASIGNADO
```json
{
  "timestamp": "2025-12-15T10:45:00.456Z",
  "tipoEvento": "TICKET_ASIGNADO",
  "actor": "sistema",
  "entityType": "Ticket",
  "entityId": "C05",
  "cambiosEstado": {
    "antes": "EN_ESPERA",
    "despues": "ATENDIENDO"
  },
  "metadata": {
    "assignedAdvisor": "Juan Pérez",
    "moduleNumber": 3
  }
}
```

#### MENSAJE_ENVIADO
```json
{
  "timestamp": "2025-12-15T10:30:05.789Z",
  "tipoEvento": "MENSAJE_ENVIADO",
  "actor": "sistema",
  "entityType": "Mensaje",
  "entityId": "123",
  "cambiosEstado": {
    "antes": "PENDIENTE",
    "despues": "ENVIADO"
  },
  "metadata": {
    "plantilla": "totem_ticket_creado",
    "telegramMessageId": "12345",
    "intentos": 1
  }
}
```

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-008 |
|-------|-------------|----------------------|
| RN-011 | Auditoría obligatoria para eventos críticos | Registrar todos los eventos listados |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Auditar creación de ticket

```gherkin
Given un cliente con nationalId "12345678-9" crea un ticket
When el sistema genera ticket C05 con status EN_ESPERA
Then el sistema registra auditoría con:
  | campo         | valor                |
  | tipoEvento    | TICKET_CREADO        |
  | actor         | cliente:12345678-9   |
  | entityType    | Ticket               |
  | entityId      | C05                  |
  | cambiosEstado | {"antes": null, "despues": "EN_ESPERA"} |
And el timestamp es preciso al milisegundo
And el registro es inmutable
```

**Validación:** Aplica RN-011 (Auditoría obligatoria)

---

#### Escenario 2: Auditar asignación de ticket a asesor

```gherkin
Given un ticket C05 está EN_ESPERA
When el sistema asigna el ticket a asesor "Juan Pérez" en módulo 3
And el ticket cambia a status ATENDIENDO
Then el sistema registra auditoría con:
  | campo         | valor                                    |
  | tipoEvento    | TICKET_ASIGNADO                          |
  | actor         | sistema                                  |
  | entityType    | Ticket                                   |
  | entityId      | C05                                      |
  | cambiosEstado | {"antes": "EN_ESPERA", "despues": "ATENDIENDO"} |
  | metadata      | {"assignedAdvisor": "Juan Pérez", "moduleNumber": 3} |
```

**Validación:** Captura cambio de estado y metadata

---

#### Escenario 3: Auditar envío exitoso de mensaje

```gherkin
Given un mensaje está PENDIENTE de envío
When el sistema envía el mensaje exitosamente
And Telegram retorna messageId "12345"
Then el sistema registra auditoría con:
  | campo         | valor                                    |
  | tipoEvento    | MENSAJE_ENVIADO                          |
  | actor         | sistema                                  |
  | entityType    | Mensaje                                  |
  | entityId      | 123                                      |
  | cambiosEstado | {"antes": "PENDIENTE", "despues": "ENVIADO"} |
  | metadata      | {"plantilla": "totem_ticket_creado", "telegramMessageId": "12345", "intentos": 1} |
```

**Validación:** Audita envío de mensajes

---

#### Escenario 4: Auditar fallo en envío de mensaje

```gherkin
Given un mensaje falló después de 4 intentos
When el sistema marca el mensaje como FALLIDO
Then el sistema registra auditoría con:
  | campo         | valor                                    |
  | tipoEvento    | MENSAJE_FALLIDO                          |
  | actor         | sistema                                  |
  | entityType    | Mensaje                                  |
  | entityId      | 124                                      |
  | cambiosEstado | {"antes": "PENDIENTE", "despues": "FALLIDO"} |
  | metadata      | {"intentos": 4, "ultimoError": "Network timeout"} |
```

**Validación:** Audita fallos con metadata de error

---

#### Escenario 5: Auditar cambio de estado de asesor

```gherkin
Given un asesor "Ana García" está AVAILABLE
When el asesor cambia a estado OFFLINE
Then el sistema registra auditoría con:
  | campo         | valor                                    |
  | tipoEvento    | ASESOR_CAMBIO_ESTADO                     |
  | actor         | asesor:ana.garcia                        |
  | entityType    | Advisor                                  |
  | entityId      | 5                                        |
  | cambiosEstado | {"antes": "AVAILABLE", "despues": "OFFLINE"} |
  | metadata      | {"moduleNumber": 5, "motivo": "Almuerzo"} |
```

**Validación:** Audita cambios de estado de asesores

---

#### Escenario 6: Auditar completación de ticket

```gherkin
Given un ticket E02 está ATENDIENDO
When el asesor "Juan Pérez" completa la atención
And el ticket cambia a status COMPLETADO
Then el sistema registra auditoría con:
  | campo         | valor                                    |
  | tipoEvento    | TICKET_COMPLETADO                        |
  | actor         | asesor:juan.perez                        |
  | entityType    | Ticket                                   |
  | entityId      | E02                                      |
  | cambiosEstado | {"antes": "ATENDIENDO", "despues": "COMPLETADO"} |
  | metadata      | {"tiempoAtencionMinutos": 18, "moduleNumber": 3} |
And el timestamp marca el momento exacto de completación
```

**Validación:** Audita completación con tiempo de atención

---

### Postcondiciones

1. Registro de auditoría insertado en base de datos
2. Registro es inmutable (no puede modificarse)
3. Timestamp preciso al milisegundo
4. Información completa capturada (actor, entidad, cambios)
5. Metadata adicional almacenada si aplica

---

### Endpoints HTTP

**Ninguno** - Este es un proceso interno automatizado.

Los registros de auditoría se crean automáticamente cuando ocurren eventos auditables.

**Nota:** Puede existir un endpoint de consulta de auditoría para administradores:
- `GET /api/admin/audit-logs?entityId={id}&tipoEvento={tipo}&desde={fecha}&hasta={fecha}`

---

## Resumen del PASO 9

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-008)
- ✅ 8 campos del modelo AuditLog
- ✅ 8 Tipos de eventos auditables
- ✅ 3 Ejemplos de información capturada (JSON completo)
- ✅ 1 Regla de negocio aplicada (RN-011)
- ✅ 6 Escenarios Gherkin
- ✅ 0 Endpoints HTTP (proceso interno)

**Cobertura de escenarios:**
- Eventos de ticket: Escenarios 1, 2, 6 (creado, asignado, completado)
- Eventos de mensaje: Escenarios 3, 4 (enviado, fallido)
- Eventos de asesor: Escenario 5 (cambio estado)

**Eventos auditables documentados:**
1. ✅ TICKET_CREADO
2. ✅ TICKET_ASIGNADO
3. ✅ TICKET_COMPLETADO
4. ✅ TICKET_CANCELADO
5. ✅ MENSAJE_ENVIADO
6. ✅ MENSAJE_FALLIDO
7. ✅ ASESOR_CAMBIO_ESTADO
8. ✅ CONSULTA_DASHBOARD

**Características clave:**
- ✅ Timestamp preciso al milisegundo
- ✅ Registros inmutables
- ✅ Captura de cambios de estado (antes/después)
- ✅ Metadata adicional por evento
- ✅ Actor identificado (cliente, asesor, sistema)

