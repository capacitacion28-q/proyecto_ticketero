# PASO 2: RF-001 - Crear Ticket Digital

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Modelo de datos con 12 campos
- ✅ 4 Reglas de negocio aplicables
- ✅ 7 Escenarios Gherkin (happy path + errores + edge cases)
- ✅ Ejemplos JSON de respuestas HTTP

---

## RF-001: Crear Ticket Digital

**Descripción:**  
El sistema debe permitir al cliente crear un ticket digital para ser atendido en sucursal, ingresando su identificación nacional (RUT/ID), número de teléfono y seleccionando el tipo de atención requerida. El sistema generará un número único de ticket, calculará la posición actual en cola y el tiempo estimado de espera basado en datos reales de la operación.

**Prioridad:** Alta

**Actor Principal:** Cliente

**Precondiciones:**
- Terminal de autoservicio disponible y funcional
- Sistema de gestión de colas operativo
- Conexión a base de datos activa

---

### Modelo de Datos (Campos del Ticket)

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| codigoReferencia | UUID | Identificador único universal | "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6" |
| numero | String | Formato [Prefijo][01-99] | "C01", "P15", "E03", "G02" |
| nationalId | String | Identificación nacional del cliente | "12345678-9" |
| telefono | String | Número de teléfono para Telegram | "+56912345678" |
| branchOffice | String | Nombre de la sucursal | "Sucursal Centro" |
| queueType | Enum | Tipo de cola | CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA |
| status | Enum | Estado del ticket | EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO |
| positionInQueue | Integer | Posición actual en cola (calculada) | 5 |
| estimatedWaitMinutes | Integer | Minutos estimados de espera | 25 |
| createdAt | Timestamp | Fecha/hora de creación | "2025-12-15T10:30:00Z" |
| assignedAdvisor | Relación | Referencia a entidad Advisor | null (inicialmente) |
| assignedModuleNumber | Integer | Número de módulo (1-5) | null (inicialmente) |

**Total de campos:** 12

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-001 |
|-------|-------------|----------------------|
| RN-001 | Un cliente solo puede tener 1 ticket activo | Validación antes de crear ticket |
| RN-005 | Formato número: [Prefijo][01-99] | Generación del campo `numero` |
| RN-006 | Prefijos: C, P, E, G | Asignación de prefijo según `queueType` |
| RN-010 | tiempoEstimado = posición × tiempoPromedio | Cálculo de `estimatedWaitMinutes` |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Creación exitosa de ticket para cola de Caja

```gherkin
Given el cliente con nationalId "12345678-9" no tiene tickets activos
And el terminal está en pantalla de selección de servicio
When el cliente ingresa:
  | Campo        | Valor           |
  | nationalId   | 12345678-9      |
  | telefono     | +56912345678    |
  | branchOffice | Sucursal Centro |
  | queueType    | CAJA            |
Then el sistema genera un ticket con:
  | Campo                 | Valor Esperado                    |
  | codigoReferencia      | UUID válido                       |
  | numero                | "C[01-99]"                        |
  | status                | EN_ESPERA                         |
  | positionInQueue       | Número > 0                        |
  | estimatedWaitMinutes  | positionInQueue × 5               |
  | assignedAdvisor       | null                              |
  | assignedModuleNumber  | null                              |
And el sistema almacena el ticket en base de datos
And el sistema programa 3 mensajes de Telegram
And el sistema retorna HTTP 201 con JSON:
```
```json
{
  "identificador": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "numero": "C01",
  "positionInQueue": 5,
  "estimatedWaitMinutes": 25,
  "queueType": "CAJA"
}
```

---

#### Escenario 2: Error - Cliente ya tiene ticket activo

```gherkin
Given el cliente con nationalId "12345678-9" tiene un ticket activo:
  | numero | status     | queueType      |
  | P05    | EN_ESPERA  | PERSONAL_BANKER|
When el cliente intenta crear un nuevo ticket con queueType CAJA
Then el sistema rechaza la creación
And el sistema retorna HTTP 409 Conflict con JSON:
```
```json
{
  "error": "TICKET_ACTIVO_EXISTENTE",
  "mensaje": "Ya tienes un ticket activo: P05",
  "ticketActivo": {
    "numero": "P05",
    "positionInQueue": 3,
    "estimatedWaitMinutes": 45
  }
}
```
```gherkin
And el sistema NO crea un nuevo ticket
```

**Validación:** Aplica RN-001 (Unicidad de Ticket Activo)

---

#### Escenario 3: Validación - RUT/ID inválido

```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa nationalId vacío
Then el sistema retorna HTTP 400 Bad Request con JSON:
```
```json
{
  "error": "VALIDACION_FALLIDA",
  "campos": {
    "nationalId": "El RUT/ID es obligatorio"
  }
}
```
```gherkin
And el sistema NO crea el ticket
```

---

#### Escenario 4: Validación - Teléfono en formato inválido

```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa telefono "123"
Then el sistema retorna HTTP 400 Bad Request
And el mensaje de error especifica formato requerido "+56XXXXXXXXX"
```

---

#### Escenario 5: Cálculo de posición - Primera persona en cola

```gherkin
Given la cola de tipo PERSONAL_BANKER está vacía
When el cliente crea un ticket para PERSONAL_BANKER
Then el sistema calcula positionInQueue = 1
And estimatedWaitMinutes = 15
And el número de ticket es "P01"
```

**Validación:** Aplica RN-010 (Cálculo: 1 × 15min = 15min)

---

#### Escenario 6: Cálculo de posición - Cola con tickets existentes

```gherkin
Given la cola de tipo EMPRESAS tiene 4 tickets EN_ESPERA
When el cliente crea un nuevo ticket para EMPRESAS
Then el sistema calcula positionInQueue = 5
And estimatedWaitMinutes = 100
And el cálculo es: 5 × 20min = 100min
```

**Validación:** Aplica RN-010 (Cálculo: 5 × 20min = 100min)

---

#### Escenario 7: Creación sin teléfono (cliente no quiere notificaciones)

```gherkin
Given el cliente no proporciona número de teléfono
When el cliente crea un ticket
Then el sistema crea el ticket exitosamente
And el sistema NO programa mensajes de Telegram
```

**Caso edge:** Cliente sin notificaciones

---

### Postcondiciones

1. Ticket almacenado en base de datos con estado EN_ESPERA
2. 3 mensajes programados en tabla Mensaje (si hay teléfono)
3. Evento de auditoría registrado: "TICKET_CREADO"
4. Respuesta HTTP 201 enviada al cliente

---

### Endpoints HTTP

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /api/tickets | Crear nuevo ticket |

**Request Body:**
```json
{
  "nationalId": "12345678-9",
  "telefono": "+56912345678",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA"
}
```

**Response 201 Created:**
```json
{
  "identificador": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "numero": "C01",
  "positionInQueue": 5,
  "estimatedWaitMinutes": 25,
  "queueType": "CAJA"
}
```

---

## Resumen del PASO 2

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-001)
- ✅ 12 campos del modelo Ticket
- ✅ 4 Reglas de negocio aplicadas (RN-001, RN-005, RN-006, RN-010)
- ✅ 7 Escenarios Gherkin
- ✅ 3 Ejemplos JSON (201, 409, 400)
- ✅ 1 Endpoint HTTP documentado

**Cobertura de escenarios:**
- Happy path: Escenario 1
- Errores de negocio: Escenario 2
- Validaciones: Escenarios 3, 4
- Cálculos: Escenarios 5, 6
- Edge cases: Escenario 7

