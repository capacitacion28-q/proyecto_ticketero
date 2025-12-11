# PASO 8: RF-007 - Panel de Monitoreo para Supervisor

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Componentes del dashboard (5 secciones)
- ✅ Métricas en tiempo real
- ✅ Alertas automáticas
- ✅ 1 Regla de negocio aplicable
- ✅ 7 Escenarios Gherkin (dashboard, alertas, actualización)
- ✅ 4 Endpoints HTTP

---

## RF-007: Panel de Monitoreo para Supervisor

**Descripción:**  
El sistema debe proveer un dashboard en tiempo real para supervisores que muestre: resumen de tickets por estado (EN_ESPERA, ATENDIENDO, COMPLETADO), cantidad de clientes en espera por cola, estado de ejecutivos (AVAILABLE, BUSY, OFFLINE), tiempos promedio de atención, y alertas de situaciones críticas (cola con más de 15 esperando, tiempo de espera mayor a 60 minutos). El dashboard debe actualizarse automáticamente cada 5 segundos sin intervención del usuario.

**Prioridad:** Alta

**Actor Principal:** Supervisor / Gerente de Sucursal

**Precondiciones:**
- Usuario con rol de supervisor autenticado
- Sistema de monitoreo operativo
- Base de datos accesible

---

### Componentes del Dashboard

#### 1. Resumen de Tickets por Estado

| Métrica | Descripción | Ejemplo |
|---------|-------------|---------|
| totalEnEspera | Tickets con status EN_ESPERA | 18 |
| totalAtendiendo | Tickets con status ATENDIENDO | 5 |
| totalCompletadosHoy | Tickets completados en el día | 142 |
| totalCanceladosHoy | Tickets cancelados en el día | 3 |
| totalNoAtendidosHoy | Tickets no atendidos en el día | 1 |

---

#### 2. Clientes en Espera por Cola

| Cola | Tickets en Espera | Tiempo Espera Promedio |
|------|-------------------|------------------------|
| CAJA | 8 | 40 min |
| PERSONAL_BANKER | 5 | 75 min |
| EMPRESAS | 3 | 60 min |
| GERENCIA | 2 | 60 min |

---

#### 3. Estado de Ejecutivos

| Métrica | Descripción | Ejemplo |
|---------|-------------|---------|
| totalAsesores | Total de asesores registrados | 10 |
| asesoresAvailable | Asesores disponibles | 3 |
| asesoresBusy | Asesores atendiendo | 5 |
| asesoresOffline | Asesores no disponibles | 2 |
| tasaOcupacion | Porcentaje de ocupación | 62.5% |

---

#### 4. Tiempos Promedio de Atención

| Cola | Tiempo Configurado | Tiempo Real Promedio | Desviación |
|------|-------------------|----------------------|------------|
| CAJA | 5 min | 5.2 min | +4% |
| PERSONAL_BANKER | 15 min | 14.5 min | -3% |
| EMPRESAS | 20 min | 22.1 min | +10% |
| GERENCIA | 30 min | 28.3 min | -6% |

---

#### 5. Alertas Automáticas

| Tipo de Alerta | Condición | Severidad |
|----------------|-----------|-----------|
| COLA_CRITICA | Más de 15 tickets en espera en una cola | Alta |
| TIEMPO_ESPERA_ALTO | Tiempo de espera > 60 minutos | Media |
| SIN_ASESORES_DISPONIBLES | Todos los asesores BUSY u OFFLINE | Crítica |
| TICKET_ABANDONADO | Ticket en espera > 90 minutos | Media |

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-007 |
|-------|-------------|----------------------|
| RN-013 | Estados de asesor (AVAILABLE, BUSY, OFFLINE) | Mostrar estado de ejecutivos |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Consultar dashboard completo

```gherkin
Given el sistema tiene:
  | ticketsEnEspera      | 18 |
  | ticketsAtendiendo    | 5  |
  | ticketsCompletadosHoy| 142|
  | asesoresAvailable    | 3  |
  | asesoresBusy         | 5  |
When el supervisor consulta GET /api/admin/dashboard
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "timestamp": "2025-12-15T11:30:00Z",
  "resumenTickets": {
    "totalEnEspera": 18,
    "totalAtendiendo": 5,
    "totalCompletadosHoy": 142,
    "totalCanceladosHoy": 3,
    "totalNoAtendidosHoy": 1
  },
  "colasPorTipo": [
    {
      "queueType": "CAJA",
      "ticketsEnEspera": 8,
      "tiempoEsperaPromedio": 40
    },
    {
      "queueType": "PERSONAL_BANKER",
      "ticketsEnEspera": 5,
      "tiempoEsperaPromedio": 75
    },
    {
      "queueType": "EMPRESAS",
      "ticketsEnEspera": 3,
      "tiempoEsperaPromedio": 60
    },
    {
      "queueType": "GERENCIA",
      "ticketsEnEspera": 2,
      "tiempoEsperaPromedio": 60
    }
  ],
  "estadoAsesores": {
    "totalAsesores": 10,
    "asesoresAvailable": 3,
    "asesoresBusy": 5,
    "asesoresOffline": 2,
    "tasaOcupacion": 62.5
  },
  "alertas": []
}
```

**Validación:** Aplica RN-013 (Estados de asesor)

---

#### Escenario 2: Dashboard con alerta de cola crítica

```gherkin
Given la cola CAJA tiene 18 tickets en espera
When el supervisor consulta GET /api/admin/dashboard
Then el sistema retorna alertas con:
```
```json
{
  "alertas": [
    {
      "tipo": "COLA_CRITICA",
      "severidad": "Alta",
      "mensaje": "Cola CAJA tiene 18 tickets en espera (límite: 15)",
      "queueType": "CAJA",
      "ticketsEnEspera": 18
    }
  ]
}
```

**Validación:** Alerta cuando tickets > 15

---

#### Escenario 3: Dashboard con alerta de tiempo de espera alto

```gherkin
Given la cola PERSONAL_BANKER tiene tiempo espera promedio de 75 minutos
When el supervisor consulta GET /api/admin/dashboard
Then el sistema retorna alertas con:
```
```json
{
  "alertas": [
    {
      "tipo": "TIEMPO_ESPERA_ALTO",
      "severidad": "Media",
      "mensaje": "Cola PERSONAL_BANKER: tiempo de espera 75 min (límite: 60 min)",
      "queueType": "PERSONAL_BANKER",
      "tiempoEsperaMinutos": 75
    }
  ]
}
```

**Validación:** Alerta cuando tiempo > 60 minutos

---

#### Escenario 4: Dashboard con alerta crítica - Sin asesores disponibles

```gherkin
Given todos los asesores están en estado BUSY u OFFLINE
And hay 10 tickets EN_ESPERA
When el supervisor consulta GET /api/admin/dashboard
Then el sistema retorna alertas con:
```
```json
{
  "alertas": [
    {
      "tipo": "SIN_ASESORES_DISPONIBLES",
      "severidad": "Crítica",
      "mensaje": "No hay asesores disponibles. 10 tickets esperando.",
      "ticketsEnEspera": 10,
      "asesoresAvailable": 0
    }
  ]
}
```

**Validación:** Alerta crítica cuando asesoresAvailable = 0

---

#### Escenario 5: Consultar resumen simplificado

```gherkin
Given el sistema tiene datos operacionales
When el supervisor consulta GET /api/admin/summary
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "ticketsEnEspera": 18,
  "ticketsAtendiendo": 5,
  "asesoresDisponibles": 3,
  "alertasActivas": 2
}
```

---

#### Escenario 6: Consultar estado de asesores detallado

```gherkin
Given hay 10 asesores registrados:
  | name        | status    | moduleNumber | assignedTicketsCount |
  | Juan Pérez  | BUSY      | 3            | 15                   |
  | Ana García  | AVAILABLE | 5            | 12                   |
  | Luis Torres | OFFLINE   | 2            | 8                    |
When el supervisor consulta GET /api/admin/advisors
Then el sistema retorna HTTP 200 con JSON:
```
```json
{
  "asesores": [
    {
      "name": "Juan Pérez",
      "status": "BUSY",
      "moduleNumber": 3,
      "assignedTicketsCount": 15,
      "ticketActual": "C10"
    },
    {
      "name": "Ana García",
      "status": "AVAILABLE",
      "moduleNumber": 5,
      "assignedTicketsCount": 12,
      "ticketActual": null
    },
    {
      "name": "Luis Torres",
      "status": "OFFLINE",
      "moduleNumber": 2,
      "assignedTicketsCount": 8,
      "ticketActual": null
    }
  ]
}
```

---

#### Escenario 7: Actualización automática cada 5 segundos

```gherkin
Given el supervisor tiene el dashboard abierto
When transcurren 5 segundos
Then el sistema ejecuta polling automático
And el dashboard se actualiza con datos frescos
And el timestamp se actualiza
And las alertas se recalculan
```

**Validación:** Actualización automática sin intervención del usuario

---

### Postcondiciones

1. Dashboard muestra datos actualizados en tiempo real
2. Alertas generadas según condiciones
3. Respuesta HTTP 200 con JSON estructurado
4. Actualización automática cada 5 segundos

---

### Endpoints HTTP

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/admin/dashboard | Dashboard completo con todas las métricas |
| GET | /api/admin/summary | Resumen simplificado |
| GET | /api/admin/advisors | Estado detallado de asesores |
| GET | /api/admin/advisors/stats | Estadísticas de asesores |

**Response 200 OK - Dashboard completo:**
```json
{
  "timestamp": "2025-12-15T11:30:00Z",
  "resumenTickets": {
    "totalEnEspera": 18,
    "totalAtendiendo": 5,
    "totalCompletadosHoy": 142,
    "totalCanceladosHoy": 3,
    "totalNoAtendidosHoy": 1
  },
  "colasPorTipo": [
    {
      "queueType": "CAJA",
      "ticketsEnEspera": 8,
      "tiempoEsperaPromedio": 40
    }
  ],
  "estadoAsesores": {
    "totalAsesores": 10,
    "asesoresAvailable": 3,
    "asesoresBusy": 5,
    "asesoresOffline": 2,
    "tasaOcupacion": 62.5
  },
  "alertas": []
}
```

**Response 200 OK - Resumen simplificado:**
```json
{
  "ticketsEnEspera": 18,
  "ticketsAtendiendo": 5,
  "asesoresDisponibles": 3,
  "alertasActivas": 2
}
```

---

## Resumen del PASO 8

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-007)
- ✅ 5 Componentes del dashboard
- ✅ 4 Tipos de alertas automáticas
- ✅ 1 Regla de negocio aplicada (RN-013)
- ✅ 7 Escenarios Gherkin
- ✅ 2 Ejemplos JSON (dashboard completo, resumen)
- ✅ 4 Endpoints HTTP documentados

**Cobertura de escenarios:**
- Dashboard completo: Escenario 1
- Alertas: Escenarios 2, 3, 4 (cola crítica, tiempo alto, sin asesores)
- Resumen: Escenario 5
- Asesores: Escenario 6
- Actualización automática: Escenario 7

**Componentes del dashboard:**
1. ✅ Resumen de tickets por estado (5 métricas)
2. ✅ Clientes en espera por cola (4 colas)
3. ✅ Estado de ejecutivos (5 métricas)
4. ✅ Tiempos promedio de atención (4 colas)
5. ✅ Alertas automáticas (4 tipos)

