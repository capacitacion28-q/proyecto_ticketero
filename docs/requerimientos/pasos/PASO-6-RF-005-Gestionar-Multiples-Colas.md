# PASO 6: RF-005 - Gestionar Múltiples Colas

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Características de las 4 colas
- ✅ Métricas por cola
- ✅ 1 Regla de negocio aplicable
- ✅ 6 Escenarios Gherkin (consultas, estadísticas)
- ✅ 2 Endpoints HTTP

---

## RF-005: Gestionar Múltiples Colas

**Descripción:**  
El sistema debe gestionar cuatro tipos de cola independientes con diferentes características: CAJA (transacciones básicas, 5 min promedio, prioridad baja), PERSONAL_BANKER (productos financieros, 15 min promedio, prioridad media), EMPRESAS (clientes corporativos, 20 min promedio, prioridad alta), y GERENCIA (casos especiales, 30 min promedio, prioridad máxima). Cada cola mantiene sus propios tickets, estadísticas y tiempos de espera independientes.

**Prioridad:** Alta

**Actor Principal:** Sistema / Supervisor

**Precondiciones:**
- Sistema de colas operativo
- Base de datos accesible
- Al menos una cola configurada

---

### Características de las 4 Colas

| Cola | Tipo de Atención | Tiempo Promedio | Prioridad | Prefijo | Casos de Uso |
|------|------------------|-----------------|-----------|---------|--------------|
| CAJA | Transacciones básicas | 5 min | 1 (baja) | C | Depósitos, retiros, pagos |
| PERSONAL_BANKER | Productos financieros | 15 min | 2 (media) | P | Créditos, inversiones, seguros |
| EMPRESAS | Clientes corporativos | 20 min | 3 (alta) | E | Cuentas empresariales, nóminas |
| GERENCIA | Casos especiales | 30 min | 4 (máxima) | G | Reclamos, casos complejos |

---

### Métricas por Cola

Cada cola mantiene las siguientes métricas en tiempo real:

| Métrica | Descripción | Ejemplo |
|---------|-------------|---------|
| ticketsEnEspera | Cantidad de tickets con status EN_ESPERA | 5 |
| ticketsAtendiendo | Cantidad de tickets con status ATENDIENDO | 2 |
| ticketsCompletadosHoy | Tickets completados en el día actual | 45 |
| tiempoPromedioAtencion | Tiempo promedio real de atención (minutos) | 5.2 |
| tiempoEsperaPromedio | Tiempo promedio de espera actual (minutos) | 25 |
| ticketMasAntiguo | Ticket más antiguo en espera | "C01" |
| tiempoEsperaMasAntiguo | Minutos que lleva esperando el más antiguo | 18 |

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-005 |
|-------|-------------|----------------------|
| RN-002 | Prioridad de colas (4 > 3 > 2 > 1) | Definición de prioridades por cola |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Consultar estado de cola CAJA

```gherkin
Given la cola CAJA tiene:
  | ticketsEnEspera      | 5  |
  | ticketsAtendiendo    | 2  |
  | ticketsCompletadosHoy| 45 |
When el supervisor consulta GET /api/admin/queues/CAJA
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "queueType": "CAJA",
  "displayName": "Caja",
  "tiempoPromedioConfiguracion": 5,
  "prioridad": 1,
  "prefijo": "C",
  "ticketsEnEspera": 5,
  "ticketsAtendiendo": 2,
  "ticketsCompletadosHoy": 45
}
```

---

#### Escenario 2: Consultar estadísticas detalladas de cola PERSONAL_BANKER

```gherkin
Given la cola PERSONAL_BANKER tiene:
  | ticketsEnEspera         | 3    |
  | tiempoPromedioAtencion  | 14.5 |
  | ticketMasAntiguo        | P05  |
  | tiempoEsperaMasAntiguo  | 22   |
When el supervisor consulta GET /api/admin/queues/PERSONAL_BANKER/stats
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "queueType": "PERSONAL_BANKER",
  "ticketsEnEspera": 3,
  "ticketsAtendiendo": 1,
  "ticketsCompletadosHoy": 28,
  "tiempoPromedioAtencion": 14.5,
  "tiempoEsperaPromedio": 43.5,
  "ticketMasAntiguo": {
    "numero": "P05",
    "tiempoEsperaMinutos": 22,
    "createdAt": "2025-12-15T10:08:00Z"
  }
}
```

---

#### Escenario 3: Cola vacía - Sin tickets en espera

```gherkin
Given la cola GERENCIA está vacía
When el supervisor consulta GET /api/admin/queues/GERENCIA
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "queueType": "GERENCIA",
  "displayName": "Gerencia",
  "tiempoPromedioConfiguracion": 30,
  "prioridad": 4,
  "prefijo": "G",
  "ticketsEnEspera": 0,
  "ticketsAtendiendo": 0,
  "ticketsCompletadosHoy": 12
}
```

---

#### Escenario 4: Comparar métricas entre colas

```gherkin
Given las 4 colas tienen tickets en espera:
  | queueType       | ticketsEnEspera | prioridad |
  | CAJA            | 8               | 1         |
  | PERSONAL_BANKER | 3               | 2         |
  | EMPRESAS        | 2               | 3         |
  | GERENCIA        | 1               | 4         |
When el supervisor consulta todas las colas
Then el sistema muestra que GERENCIA tiene mayor prioridad
And CAJA tiene mayor cantidad de tickets en espera
```

**Validación:** Aplica RN-002 (Prioridades diferentes por cola)

---

#### Escenario 5: Cola EMPRESAS con alerta de tiempo de espera alto

```gherkin
Given la cola EMPRESAS tiene:
  | ticketsEnEspera        | 6   |
  | tiempoEsperaPromedio   | 120 |
  | ticketMasAntiguo       | E01 |
  | tiempoEsperaMasAntiguo | 45  |
When el sistema evalúa alertas
Then el sistema genera alerta "TIEMPO_ESPERA_ALTO"
And la alerta indica: "Cola EMPRESAS: tiempo espera promedio 120 min"
```

**Validación:** Monitoreo de tiempos críticos

---

#### Escenario 6: Consultar cola inexistente - Error 400

```gherkin
Given no existe cola con tipo "INVALIDA"
When el supervisor consulta GET /api/admin/queues/INVALIDA
Then el sistema retorna HTTP 400 con JSON:
```
```json
{
  "error": "COLA_INVALIDA",
  "mensaje": "Tipo de cola inválido: INVALIDA",
  "colasValidas": ["CAJA", "PERSONAL_BANKER", "EMPRESAS", "GERENCIA"]
}
```

---

### Postcondiciones

1. Métricas calculadas en tiempo real
2. Estadísticas actualizadas por cola
3. Respuesta HTTP 200 con datos actuales
4. Alertas generadas si aplica

---

### Endpoints HTTP

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/admin/queues/{type} | Consultar estado de una cola específica |
| GET | /api/admin/queues/{type}/stats | Consultar estadísticas detalladas de una cola |

**Response 200 OK - Estado básico:**
```json
{
  "queueType": "CAJA",
  "displayName": "Caja",
  "tiempoPromedioConfiguracion": 5,
  "prioridad": 1,
  "prefijo": "C",
  "ticketsEnEspera": 5,
  "ticketsAtendiendo": 2,
  "ticketsCompletadosHoy": 45
}
```

**Response 200 OK - Estadísticas detalladas:**
```json
{
  "queueType": "PERSONAL_BANKER",
  "ticketsEnEspera": 3,
  "ticketsAtendiendo": 1,
  "ticketsCompletadosHoy": 28,
  "tiempoPromedioAtencion": 14.5,
  "tiempoEsperaPromedio": 43.5,
  "ticketMasAntiguo": {
    "numero": "P05",
    "tiempoEsperaMinutos": 22,
    "createdAt": "2025-12-15T10:08:00Z"
  }
}
```

**Response 400 Bad Request:**
```json
{
  "error": "COLA_INVALIDA",
  "mensaje": "Tipo de cola inválido: INVALIDA",
  "colasValidas": ["CAJA", "PERSONAL_BANKER", "EMPRESAS", "GERENCIA"]
}
```

---

## Resumen del PASO 6

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-005)
- ✅ Tabla de características (4 colas)
- ✅ Tabla de métricas (7 métricas por cola)
- ✅ 1 Regla de negocio aplicada (RN-002)
- ✅ 6 Escenarios Gherkin
- ✅ 3 Ejemplos JSON (200 básico, 200 detallado, 400)
- ✅ 2 Endpoints HTTP documentados

**Cobertura de escenarios:**
- Consulta básica: Escenario 1
- Estadísticas detalladas: Escenario 2
- Cola vacía: Escenario 3
- Comparación entre colas: Escenario 4
- Alertas: Escenario 5
- Error handling: Escenario 6

**Colas documentadas:**
1. ✅ CAJA (5 min, prioridad 1, prefijo C)
2. ✅ PERSONAL_BANKER (15 min, prioridad 2, prefijo P)
3. ✅ EMPRESAS (20 min, prioridad 3, prefijo E)
4. ✅ GERENCIA (30 min, prioridad 4, prefijo G)

