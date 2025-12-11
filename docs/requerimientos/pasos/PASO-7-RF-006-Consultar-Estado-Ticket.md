# PASO 7: RF-006 - Consultar Estado del Ticket

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Información retornada según estado del ticket
- ✅ 1 Regla de negocio aplicable
- ✅ 6 Escenarios Gherkin (diferentes estados y consultas)
- ✅ 2 Endpoints HTTP

---

## RF-006: Consultar Estado del Ticket

**Descripción:**  
El sistema debe permitir al cliente consultar en cualquier momento el estado de su ticket mediante el código de referencia (UUID) o el número de ticket, mostrando: estado actual, posición en cola (si aplica), tiempo estimado actualizado, ejecutivo asignado (si aplica), módulo asignado (si aplica), y hora de creación. La información debe actualizarse en tiempo real reflejando cambios en la cola.

**Prioridad:** Alta

**Actor Principal:** Cliente / Sistema

**Precondiciones:**
- Ticket existe en el sistema
- Base de datos accesible
- Sistema de consultas operativo

---

### Información Retornada Según Estado

| Estado | Información Incluida |
|--------|---------------------|
| EN_ESPERA | numero, status, positionInQueue, estimatedWaitMinutes, queueType, createdAt |
| PROXIMO | numero, status, positionInQueue (≤3), estimatedWaitMinutes, queueType, mensaje "Acércate a sucursal" |
| ATENDIENDO | numero, status, assignedAdvisor, assignedModuleNumber, queueType, createdAt |
| COMPLETADO | numero, status, assignedAdvisor, assignedModuleNumber, completedAt |
| CANCELADO | numero, status, canceledAt, motivoCancelacion |
| NO_ATENDIDO | numero, status, assignedModuleNumber, noShowAt |

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-006 |
|-------|-------------|----------------------|
| RN-009 | Estados de ticket (6 estados posibles) | Validar y retornar estado actual |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Consultar ticket EN_ESPERA por UUID

```gherkin
Given existe un ticket con:
  | codigoReferencia     | a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6 |
  | numero               | C05                                  |
  | status               | EN_ESPERA                            |
  | positionInQueue      | 5                                    |
  | estimatedWaitMinutes | 25                                   |
  | queueType            | CAJA                                 |
When el cliente consulta GET /api/tickets/a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "codigoReferencia": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "numero": "C05",
  "status": "EN_ESPERA",
  "positionInQueue": 5,
  "estimatedWaitMinutes": 25,
  "queueType": "CAJA",
  "createdAt": "2025-12-15T10:30:00Z"
}
```

**Validación:** Aplica RN-009 (Estado EN_ESPERA)

---

#### Escenario 2: Consultar ticket PROXIMO por número

```gherkin
Given existe un ticket con:
  | numero               | P03       |
  | status               | PROXIMO   |
  | positionInQueue      | 2         |
  | estimatedWaitMinutes | 30        |
When el cliente consulta GET /api/tickets/P03/position
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "numero": "P03",
  "status": "PROXIMO",
  "positionInQueue": 2,
  "estimatedWaitMinutes": 30,
  "queueType": "PERSONAL_BANKER",
  "mensaje": "¡Pronto será tu turno! Acércate a la sucursal."
}
```

**Validación:** Aplica RN-009 (Estado PROXIMO) y RN-012 (posición ≤ 3)

---

#### Escenario 3: Consultar ticket ATENDIENDO

```gherkin
Given existe un ticket con:
  | numero               | E02        |
  | status               | ATENDIENDO |
  | assignedAdvisor      | Juan Pérez |
  | assignedModuleNumber | 3          |
When el cliente consulta GET /api/tickets/E02/position
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "numero": "E02",
  "status": "ATENDIENDO",
  "queueType": "EMPRESAS",
  "assignedAdvisor": {
    "name": "Juan Pérez",
    "moduleNumber": 3
  },
  "createdAt": "2025-12-15T10:15:00Z",
  "assignedAt": "2025-12-15T10:45:00Z"
}
```

**Validación:** Aplica RN-009 (Estado ATENDIENDO)

---

#### Escenario 4: Consultar ticket COMPLETADO

```gherkin
Given existe un ticket con:
  | numero               | G01        |
  | status               | COMPLETADO |
  | assignedAdvisor      | Ana García |
  | assignedModuleNumber | 5          |
  | completedAt          | 2025-12-15T11:30:00Z |
When el cliente consulta GET /api/tickets/G01/position
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "numero": "G01",
  "status": "COMPLETADO",
  "queueType": "GERENCIA",
  "assignedAdvisor": {
    "name": "Ana García",
    "moduleNumber": 5
  },
  "createdAt": "2025-12-15T10:00:00Z",
  "completedAt": "2025-12-15T11:30:00Z",
  "tiempoTotalAtencion": 90
}
```

**Validación:** Aplica RN-009 (Estado COMPLETADO)

---

#### Escenario 5: Consultar ticket inexistente - Error 404

```gherkin
Given no existe ticket con numero "X99"
When el cliente consulta GET /api/tickets/X99/position
Then el sistema retorna HTTP 404 con JSON:
```
```json
{
  "error": "TICKET_NO_ENCONTRADO",
  "mensaje": "No existe ticket con número X99"
}
```

---

#### Escenario 6: Consultar con UUID inválido - Error 400

```gherkin
Given el cliente proporciona UUID inválido "abc123"
When el cliente consulta GET /api/tickets/abc123
Then el sistema retorna HTTP 400 con JSON:
```
```json
{
  "error": "UUID_INVALIDO",
  "mensaje": "El formato del UUID es inválido: abc123"
}
```

---

### Postcondiciones

1. Información del ticket retornada según su estado actual
2. Datos actualizados en tiempo real
3. Respuesta HTTP 200 con JSON estructurado
4. Consulta registrada en logs (opcional)

---

### Endpoints HTTP

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/tickets/{codigoReferencia} | Consultar ticket por UUID |
| GET | /api/tickets/{numero}/position | Consultar ticket por número |

**Response 200 OK - Ticket EN_ESPERA:**
```json
{
  "codigoReferencia": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "numero": "C05",
  "status": "EN_ESPERA",
  "positionInQueue": 5,
  "estimatedWaitMinutes": 25,
  "queueType": "CAJA",
  "createdAt": "2025-12-15T10:30:00Z"
}
```

**Response 200 OK - Ticket ATENDIENDO:**
```json
{
  "numero": "E02",
  "status": "ATENDIENDO",
  "queueType": "EMPRESAS",
  "assignedAdvisor": {
    "name": "Juan Pérez",
    "moduleNumber": 3
  },
  "createdAt": "2025-12-15T10:15:00Z",
  "assignedAt": "2025-12-15T10:45:00Z"
}
```

**Response 404 Not Found:**
```json
{
  "error": "TICKET_NO_ENCONTRADO",
  "mensaje": "No existe ticket con número X99"
}
```

**Response 400 Bad Request:**
```json
{
  "error": "UUID_INVALIDO",
  "mensaje": "El formato del UUID es inválido: abc123"
}
```

---

## Resumen del PASO 7

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-006)
- ✅ Tabla de información por estado (6 estados)
- ✅ 1 Regla de negocio aplicada (RN-009)
- ✅ 6 Escenarios Gherkin
- ✅ 4 Ejemplos JSON (200 EN_ESPERA, 200 ATENDIENDO, 404, 400)
- ✅ 2 Endpoints HTTP documentados

**Cobertura de escenarios:**
- Estados activos: Escenarios 1, 2, 3 (EN_ESPERA, PROXIMO, ATENDIENDO)
- Estados finales: Escenario 4 (COMPLETADO)
- Error handling: Escenarios 5, 6 (404, 400)

**Estados documentados:**
1. ✅ EN_ESPERA (con posición y tiempo)
2. ✅ PROXIMO (con mensaje de acercarse)
3. ✅ ATENDIENDO (con asesor y módulo)
4. ✅ COMPLETADO (con tiempos totales)
5. ✅ CANCELADO (mencionado en tabla)
6. ✅ NO_ATENDIDO (mencionado en tabla)

