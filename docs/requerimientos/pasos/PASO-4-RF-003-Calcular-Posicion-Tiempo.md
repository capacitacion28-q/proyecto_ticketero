# PASO 4: RF-003 - Calcular Posición y Tiempo Estimado

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Algoritmos de cálculo (posición y tiempo)
- ✅ Tabla de tiempos promedio por cola
- ✅ 2 Reglas de negocio aplicables
- ✅ 6 Escenarios Gherkin (cálculos en diferentes situaciones)
- ✅ 1 Endpoint HTTP

---

## RF-003: Calcular Posición y Tiempo Estimado

**Descripción:**  
El sistema debe calcular en tiempo real la posición exacta del cliente en cola y estimar el tiempo de espera basado en: posición actual, tiempo promedio de atención por tipo de cola, y cantidad de tickets en espera. El cálculo debe actualizarse dinámicamente cuando otros tickets son atendidos o cancelados.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Ticket existe en el sistema
- Cola del tipo especificado está operativa
- Base de datos accesible para consultas

---

### Algoritmos de Cálculo

#### Algoritmo 1: Cálculo de Posición en Cola

```
posición = COUNT(tickets EN_ESPERA creados antes de este ticket) + 1
```

**Criterios:**
- Solo contar tickets con `status = EN_ESPERA`
- Solo contar tickets de la misma `queueType`
- Ordenar por `createdAt` ascendente (FIFO)
- El ticket más antiguo tiene posición = 1

**Ejemplo:**
```
Cola CAJA:
- Ticket C01 (createdAt: 10:00) → posición = 1
- Ticket C02 (createdAt: 10:05) → posición = 2
- Ticket C03 (createdAt: 10:10) → posición = 3
```

---

#### Algoritmo 2: Cálculo de Tiempo Estimado

```
tiempoEstimado = posición × tiempoPromedioCola
```

**Tabla de Tiempos Promedio:**

| QueueType | Tiempo Promedio | Ejemplo Cálculo |
|-----------|-----------------|-----------------|
| CAJA | 5 minutos | posición 5 → 5 × 5 = 25 min |
| PERSONAL_BANKER | 15 minutos | posición 3 → 3 × 15 = 45 min |
| EMPRESAS | 20 minutos | posición 4 → 4 × 20 = 80 min |
| GERENCIA | 30 minutos | posición 2 → 2 × 30 = 60 min |

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-003 |
|-------|-------------|----------------------|
| RN-003 | Orden FIFO dentro de cola | Ordenar por `createdAt` para calcular posición |
| RN-010 | tiempoEstimado = posición × tiempoPromedio | Fórmula de cálculo de tiempo |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Cálculo de posición - Primera persona en cola vacía

```gherkin
Given la cola de tipo CAJA está vacía
When un cliente crea un ticket para CAJA
Then el sistema calcula:
  | campo                | valor |
  | positionInQueue      | 1     |
  | estimatedWaitMinutes | 5     |
And el cálculo es: 1 × 5min = 5min
```

**Validación:** Aplica RN-010 (1 × 5 = 5)

---

#### Escenario 2: Cálculo de posición - Cola con 3 tickets existentes

```gherkin
Given la cola PERSONAL_BANKER tiene 3 tickets EN_ESPERA:
  | numero | createdAt |
  | P01    | 10:00     |
  | P02    | 10:05     |
  | P03    | 10:10     |
When un cliente crea ticket P04 a las 10:15
Then el sistema calcula:
  | campo                | valor |
  | positionInQueue      | 4     |
  | estimatedWaitMinutes | 60    |
And el cálculo es: 4 × 15min = 60min
```

**Validación:** Aplica RN-003 (FIFO) y RN-010 (4 × 15 = 60)

---

#### Escenario 3: Recálculo cuando ticket anterior es atendido

```gherkin
Given un ticket E05 tiene positionInQueue = 5
And hay 4 tickets adelante en cola EMPRESAS
When el ticket E01 (posición 1) cambia a status ATENDIENDO
Then el sistema recalcula para E05:
  | campo                | valor |
  | positionInQueue      | 4     |
  | estimatedWaitMinutes | 80    |
And el cálculo es: 4 × 20min = 80min
```

**Validación:** Recálculo dinámico al cambiar estado de otros tickets

---

#### Escenario 4: Recálculo cuando ticket anterior es cancelado

```gherkin
Given un ticket G03 tiene positionInQueue = 3
And hay 2 tickets adelante en cola GERENCIA
When el ticket G01 (posición 1) cambia a status CANCELADO
Then el sistema recalcula para G03:
  | campo                | valor |
  | positionInQueue      | 2     |
  | estimatedWaitMinutes | 60    |
And el cálculo es: 2 × 30min = 60min
```

**Validación:** Recálculo al cancelar tickets

---

#### Escenario 5: Consulta de posición vía API

```gherkin
Given un ticket C10 existe con:
  | numero              | C10 |
  | positionInQueue     | 7   |
  | estimatedWaitMinutes| 35  |
When el cliente consulta GET /api/tickets/C10/position
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "numero": "C10",
  "positionInQueue": 7,
  "estimatedWaitMinutes": 35,
  "queueType": "CAJA",
  "status": "EN_ESPERA"
}
```

---

#### Escenario 6: Ticket no existe - Error 404

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

### Postcondiciones

1. Posición calculada correctamente según orden FIFO
2. Tiempo estimado calculado según fórmula
3. Valores actualizados en base de datos
4. Respuesta HTTP 200 con datos actualizados

---

### Endpoints HTTP

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/tickets/{numero}/position | Consultar posición y tiempo estimado |

**Response 200 OK:**
```json
{
  "numero": "C10",
  "positionInQueue": 7,
  "estimatedWaitMinutes": 35,
  "queueType": "CAJA",
  "status": "EN_ESPERA"
}
```

**Response 404 Not Found:**
```json
{
  "error": "TICKET_NO_ENCONTRADO",
  "mensaje": "No existe ticket con número X99"
}
```

---

## Resumen del PASO 4

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-003)
- ✅ 2 Algoritmos de cálculo (posición y tiempo)
- ✅ Tabla de tiempos promedio (4 tipos de cola)
- ✅ 2 Reglas de negocio aplicadas (RN-003, RN-010)
- ✅ 6 Escenarios Gherkin
- ✅ 2 Ejemplos JSON (200, 404)
- ✅ 1 Endpoint HTTP documentado

**Cobertura de escenarios:**
- Cálculo inicial: Escenarios 1, 2
- Recálculo dinámico: Escenarios 3, 4
- Consulta API: Escenario 5
- Error handling: Escenario 6

**Fórmulas documentadas:**
1. ✅ posición = COUNT(tickets EN_ESPERA antes) + 1
2. ✅ tiempoEstimado = posición × tiempoPromedioCola

