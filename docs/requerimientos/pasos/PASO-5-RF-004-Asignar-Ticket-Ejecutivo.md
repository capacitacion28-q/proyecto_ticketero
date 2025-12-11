# PASO 5: RF-004 - Asignar Ticket a Ejecutivo Automáticamente

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Modelo de datos Advisor con 5 campos
- ✅ Algoritmo de asignación automática
- ✅ Tabla de prioridades de colas
- ✅ 3 Reglas de negocio aplicables
- ✅ 8 Escenarios Gherkin (asignación, prioridades, balanceo)

---

## RF-004: Asignar Ticket a Ejecutivo Automáticamente

**Descripción:**  
El sistema debe asignar automáticamente el siguiente ticket en cola cuando un ejecutivo se libere (cambia a estado AVAILABLE), considerando: prioridad de colas (GERENCIA > EMPRESAS > PERSONAL_BANKER > CAJA), balanceo de carga entre ejecutivos disponibles (seleccionar el que tiene menos tickets asignados), y orden FIFO dentro de cada cola. Al asignar, el sistema actualiza el ticket con el asesor y módulo, cambia el estado del asesor a BUSY, y envía el Mensaje 3 al cliente.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Al menos un asesor en estado AVAILABLE
- Al menos un ticket en estado EN_ESPERA
- Sistema de asignación activo

---

### Modelo de Datos (Entidad Advisor)

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| id | BIGSERIAL | Primary key | 1 |
| name | String | Nombre completo del asesor | "Juan Pérez" |
| email | String | Email corporativo | "juan.perez@banco.com" |
| status | Enum | Estado actual | AVAILABLE, BUSY, OFFLINE |
| moduleNumber | Integer | Número de módulo (1-5) | 3 |
| assignedTicketsCount | Integer | Contador de tickets asignados hoy | 12 |

**Total de campos:** 6

---

### Algoritmo de Asignación Automática

**Paso 1: Verificar asesor disponible**
```
IF NOT EXISTS (Advisor WHERE status = AVAILABLE)
  THEN return (no hay asesores disponibles)
```

**Paso 2: Seleccionar ticket según prioridad de cola**
```
SELECT ticket 
FROM tickets 
WHERE status = EN_ESPERA
ORDER BY 
  queueType.priority DESC,  -- Mayor prioridad primero
  createdAt ASC             -- FIFO dentro de cola
LIMIT 1
```

**Paso 3: Seleccionar asesor con menor carga**
```
SELECT advisor
FROM advisors
WHERE status = AVAILABLE
ORDER BY assignedTicketsCount ASC
LIMIT 1
```

**Paso 4: Realizar asignación**
```
UPDATE ticket SET
  assignedAdvisor = advisor.id,
  assignedModuleNumber = advisor.moduleNumber,
  status = ATENDIENDO

UPDATE advisor SET
  status = BUSY,
  assignedTicketsCount = assignedTicketsCount + 1

INSERT INTO mensaje (plantilla = totem_es_tu_turno)
```

---

### Tabla de Prioridades de Colas

| QueueType | Prioridad | Orden de Asignación |
|-----------|-----------|---------------------|
| GERENCIA | 4 (máxima) | 1° |
| EMPRESAS | 3 | 2° |
| PERSONAL_BANKER | 2 | 3° |
| CAJA | 1 (mínima) | 4° |

**Ejemplo:**
```
Cola actual:
- 2 tickets CAJA (prioridad 1)
- 1 ticket GERENCIA (prioridad 4)
- 3 tickets PERSONAL_BANKER (prioridad 2)

Orden de asignación:
1. Ticket GERENCIA (prioridad 4)
2. Tickets PERSONAL_BANKER (prioridad 2, FIFO)
3. Tickets CAJA (prioridad 1, FIFO)
```

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-004 |
|-------|-------------|----------------------|
| RN-002 | Prioridad de colas (4 > 3 > 2 > 1) | Ordenar tickets por prioridad |
| RN-003 | Orden FIFO dentro de cola | Desempate por createdAt |
| RN-004 | Balanceo de carga | Seleccionar asesor con menor assignedTicketsCount |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Asignación exitosa - Asesor disponible y ticket en espera

```gherkin
Given existe un asesor disponible:
  | name       | Juan Pérez |
  | status     | AVAILABLE  |
  | moduleNumber | 3        |
  | assignedTicketsCount | 5 |
And existe un ticket en espera:
  | numero    | C05       |
  | status    | EN_ESPERA |
  | queueType | CAJA      |
When el sistema ejecuta asignación automática
Then el sistema asigna el ticket al asesor
And el ticket se actualiza con:
  | assignedAdvisor      | Juan Pérez |
  | assignedModuleNumber | 3          |
  | status               | ATENDIENDO |
And el asesor se actualiza con:
  | status               | BUSY |
  | assignedTicketsCount | 6    |
And el sistema programa Mensaje 3 (totem_es_tu_turno)
```

**Validación:** Asignación básica exitosa

---

#### Escenario 2: Prioridad de colas - GERENCIA antes que CAJA

```gherkin
Given hay 2 tickets en espera:
  | numero | queueType | createdAt | prioridad |
  | C01    | CAJA      | 10:00     | 1         |
  | G01    | GERENCIA  | 10:05     | 4         |
And hay 1 asesor AVAILABLE
When el sistema ejecuta asignación automática
Then el sistema asigna ticket G01 (GERENCIA)
And el ticket C01 (CAJA) permanece EN_ESPERA
```

**Validación:** Aplica RN-002 (Prioridad GERENCIA > CAJA)

---

#### Escenario 3: FIFO dentro de misma cola

```gherkin
Given hay 3 tickets PERSONAL_BANKER en espera:
  | numero | createdAt |
  | P01    | 10:00     |
  | P02    | 10:05     |
  | P03    | 10:10     |
And hay 1 asesor AVAILABLE
When el sistema ejecuta asignación automática
Then el sistema asigna ticket P01 (más antiguo)
And P02 y P03 permanecen EN_ESPERA
```

**Validación:** Aplica RN-003 (FIFO)

---

#### Escenario 4: Balanceo de carga - Seleccionar asesor con menos tickets

```gherkin
Given hay 3 asesores AVAILABLE:
  | name        | assignedTicketsCount |
  | Juan Pérez  | 10                   |
  | Ana García  | 5                    |
  | Luis Torres | 8                    |
And hay 1 ticket EN_ESPERA
When el sistema ejecuta asignación automática
Then el sistema selecciona asesor "Ana García" (menor carga: 5)
And Ana García.assignedTicketsCount = 6
```

**Validación:** Aplica RN-004 (Balanceo de carga)

---

#### Escenario 5: No hay asesores disponibles - No se asigna

```gherkin
Given todos los asesores están en estado BUSY o OFFLINE
And hay 5 tickets EN_ESPERA
When el sistema ejecuta asignación automática
Then el sistema NO asigna ningún ticket
And todos los tickets permanecen EN_ESPERA
```

**Validación:** Manejo de caso sin asesores disponibles

---

#### Escenario 6: No hay tickets en espera - No se asigna

```gherkin
Given hay 2 asesores AVAILABLE
And NO hay tickets EN_ESPERA
When el sistema ejecuta asignación automática
Then el sistema NO realiza ninguna asignación
And los asesores permanecen AVAILABLE
```

**Validación:** Manejo de caso sin tickets

---

#### Escenario 7: Múltiples asignaciones - Prioridad y balanceo combinados

```gherkin
Given hay 4 tickets en espera:
  | numero | queueType       | createdAt | prioridad |
  | C01    | CAJA            | 10:00     | 1         |
  | E01    | EMPRESAS        | 10:02     | 3         |
  | P01    | PERSONAL_BANKER | 10:03     | 2         |
  | G01    | GERENCIA        | 10:05     | 4         |
And hay 2 asesores AVAILABLE:
  | name       | assignedTicketsCount |
  | Juan Pérez | 3                    |
  | Ana García | 3                    |
When el sistema ejecuta asignación automática (1ra vez)
Then el sistema asigna G01 a Juan Pérez (prioridad 4, empate en carga)
When el sistema ejecuta asignación automática (2da vez)
Then el sistema asigna E01 a Ana García (prioridad 3, menor carga)
```

**Validación:** Aplica RN-002, RN-003 y RN-004 combinadas

---

#### Escenario 8: Asesor se libera - Trigger de asignación automática

```gherkin
Given un asesor está BUSY atendiendo ticket C05
And hay 3 tickets EN_ESPERA
When el asesor completa la atención
And el asesor cambia a status AVAILABLE
Then el sistema detecta el cambio de estado
And el sistema ejecuta asignación automática inmediatamente
And el siguiente ticket EN_ESPERA es asignado al asesor
```

**Validación:** Trigger automático al liberar asesor

---

### Postcondiciones

1. Ticket actualizado con asesor y módulo asignado
2. Estado del ticket cambiado a ATENDIENDO
3. Estado del asesor cambiado a BUSY
4. Contador assignedTicketsCount incrementado
5. Mensaje 3 programado para envío
6. Auditoría registrada: "TICKET_ASIGNADO"

---

### Endpoints HTTP

**Ninguno** - Este es un proceso interno automatizado.

El sistema monitorea cambios de estado de asesores y ejecuta asignación automáticamente.

---

## Resumen del PASO 5

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-004)
- ✅ 6 campos del modelo Advisor
- ✅ Algoritmo de asignación (4 pasos)
- ✅ Tabla de prioridades (4 colas)
- ✅ 3 Reglas de negocio aplicadas (RN-002, RN-003, RN-004)
- ✅ 8 Escenarios Gherkin
- ✅ 0 Endpoints HTTP (proceso interno)

**Cobertura de escenarios:**
- Happy path: Escenario 1
- Prioridad de colas: Escenarios 2, 7
- FIFO: Escenario 3
- Balanceo de carga: Escenarios 4, 7
- Edge cases: Escenarios 5, 6
- Trigger automático: Escenario 8

**Algoritmo documentado:**
1. ✅ Verificar asesor disponible
2. ✅ Seleccionar ticket (prioridad + FIFO)
3. ✅ Seleccionar asesor (menor carga)
4. ✅ Realizar asignación (actualizar ticket, asesor, mensaje)

