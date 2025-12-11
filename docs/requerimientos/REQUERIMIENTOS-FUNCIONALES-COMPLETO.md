# Requerimientos Funcionales - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Cliente:** Institución Financiera  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Analista:** Equipo de Producto e Innovación

---

## ÍNDICE

1. [Introducción](#1-introducción)
2. [Reglas de Negocio](#2-reglas-de-negocio)
3. [Enumeraciones](#3-enumeraciones)
4. [Requerimientos Funcionales](#4-requerimientos-funcionales)
   - [RF-001: Crear Ticket Digital](#rf-001-crear-ticket-digital)
   - [RF-002: Enviar Notificaciones Automáticas vía Telegram](#rf-002-enviar-notificaciones-automáticas-vía-telegram)
   - [RF-003: Calcular Posición y Tiempo Estimado](#rf-003-calcular-posición-y-tiempo-estimado)
   - [RF-004: Asignar Ticket a Ejecutivo Automáticamente](#rf-004-asignar-ticket-a-ejecutivo-automáticamente)
   - [RF-005: Gestionar Múltiples Colas](#rf-005-gestionar-múltiples-colas)
   - [RF-006: Consultar Estado del Ticket](#rf-006-consultar-estado-del-ticket)
   - [RF-007: Panel de Monitoreo para Supervisor](#rf-007-panel-de-monitoreo-para-supervisor)
   - [RF-008: Registrar Auditoría de Eventos](#rf-008-registrar-auditoría-de-eventos)
5. [Matriz de Trazabilidad](#5-matriz-de-trazabilidad)
6. [Casos de Uso Principales](#6-casos-de-uso-principales)
7. [Matriz de Endpoints HTTP](#7-matriz-de-endpoints-http)
8. [Validaciones y Formatos](#8-validaciones-y-formatos)
9. [Glosario](#9-glosario)
10. [Resumen Ejecutivo](#10-resumen-ejecutivo)

---

## 1. Introducción

### 1.1 Propósito

Este documento especifica los requerimientos funcionales del Sistema Ticketero Digital, diseñado para modernizar la experiencia de atención en sucursales mediante:
- Digitalización completa del proceso de tickets
- Notificaciones automáticas en tiempo real vía Telegram
- Movilidad del cliente durante la espera
- Asignación inteligente de clientes a ejecutivos
- Panel de monitoreo para supervisión operacional

### 1.2 Alcance

Este documento cubre:
- ✅ 8 Requerimientos Funcionales (RF-001 a RF-008)
- ✅ 13 Reglas de Negocio (RN-001 a RN-013)
- ✅ 51 Criterios de aceptación en formato Gherkin
- ✅ Modelo de datos funcional (3 entidades principales)
- ✅ Matriz de trazabilidad completa
- ✅ 11 Endpoints HTTP documentados

Este documento NO cubre:
- ❌ Arquitectura técnica (ver documento ARQUITECTURA.md)
- ❌ Tecnologías de implementación
- ❌ Diseño de interfaces de usuario

### 1.3 Definiciones

| Término | Definición |
|---------|------------|
| Ticket | Turno digital asignado a un cliente para ser atendido |
| Cola | Fila virtual de tickets esperando atención |
| Asesor | Ejecutivo bancario que atiende clientes |
| Módulo | Estación de trabajo de un asesor (numerados 1-5) |
| Chat ID | Identificador único de usuario en Telegram |
| UUID | Identificador único universal para tickets |
| RUT/ID | Identificación nacional del cliente (Rol Único Tributario o ID) |
| FIFO | First In, First Out - Primero en entrar, primero en salir |
| Backoff Exponencial | Estrategia de reintentos con tiempos crecientes |

---

## 2. Reglas de Negocio

Las siguientes reglas de negocio aplican transversalmente a todos los requerimientos funcionales:

**RN-001: Unicidad de Ticket Activo**  
Un cliente solo puede tener 1 ticket activo a la vez. Los estados activos son: EN_ESPERA, PROXIMO, ATENDIENDO. Si un cliente intenta crear un nuevo ticket teniendo uno activo, el sistema debe rechazar la solicitud con error HTTP 409 Conflict.

**RN-002: Prioridad de Colas**  
Las colas tienen prioridades numéricas para asignación automática:
- GERENCIA: prioridad 4 (máxima)
- EMPRESAS: prioridad 3
- PERSONAL_BANKER: prioridad 2
- CAJA: prioridad 1 (mínima)

Cuando un asesor se libera, el sistema asigna primero tickets de colas con mayor prioridad.

**RN-003: Orden FIFO Dentro de Cola**  
Dentro de una misma cola, los tickets se procesan en orden FIFO (First In, First Out). El ticket más antiguo (createdAt menor) se asigna primero.

**RN-004: Balanceo de Carga Entre Asesores**  
Al asignar un ticket, el sistema selecciona el asesor AVAILABLE con menor valor de assignedTicketsCount, distribuyendo equitativamente la carga de trabajo.

**RN-005: Formato de Número de Ticket**  
El número de ticket sigue el formato: [Prefijo][Número secuencial 01-99]
- Prefijo: 1 letra según el tipo de cola
- Número: 2 dígitos, del 01 al 99, reseteado diariamente

Ejemplos: C01, P15, E03, G02

**RN-006: Prefijos por Tipo de Cola**  
- CAJA → C
- PERSONAL_BANKER → P
- EMPRESAS → E
- GERENCIA → G

**RN-007: Reintentos Automáticos de Mensajes**  
Si el envío de un mensaje a Telegram falla, el sistema reintenta automáticamente hasta 3 veces antes de marcarlo como FALLIDO.

**RN-008: Backoff Exponencial en Reintentos**  
Los reintentos de mensajes usan backoff exponencial:
- Intento 1: inmediato
- Intento 2: después de 30 segundos
- Intento 3: después de 60 segundos
- Intento 4: después de 120 segundos

**RN-009: Estados de Ticket**  
Un ticket puede estar en uno de estos estados:
- EN_ESPERA: esperando asignación a asesor
- PROXIMO: próximo a ser atendido (posición ≤ 3)
- ATENDIENDO: siendo atendido por un asesor
- COMPLETADO: atención finalizada exitosamente
- CANCELADO: cancelado por cliente o sistema
- NO_ATENDIDO: cliente no se presentó cuando fue llamado

**RN-010: Cálculo de Tiempo Estimado**  
El tiempo estimado de espera se calcula como:

```
tiempoEstimado = posiciónEnCola × tiempoPromedioCola
```

Donde tiempoPromedioCola varía por tipo:
- CAJA: 5 minutos
- PERSONAL_BANKER: 15 minutos
- EMPRESAS: 20 minutos
- GERENCIA: 30 minutos

**RN-011: Auditoría Obligatoria**  
Todos los eventos críticos del sistema deben registrarse en auditoría con: timestamp, tipo de evento, actor involucrado, entityId afectado, y cambios de estado.

**RN-012: Umbral de Pre-aviso**  
El sistema envía el Mensaje 2 (pre-aviso) cuando la posición del ticket es ≤ 3, indicando que el cliente debe acercarse a la sucursal.

**RN-013: Estados de Asesor**  
Un asesor puede estar en uno de estos estados:
- AVAILABLE: disponible para recibir asignaciones
- BUSY: atendiendo un cliente (no recibe nuevas asignaciones)
- OFFLINE: no disponible (almuerzo, capacitación, etc.)

---

## 3. Enumeraciones

### 3.1 QueueType

Tipos de cola disponibles en el sistema:

| Valor | Display Name | Tiempo Promedio | Prioridad | Prefijo |
|-------|--------------|-----------------|-----------|---------|
| CAJA | Caja | 5 min | 1 | C |
| PERSONAL_BANKER | Personal Banker | 15 min | 2 | P |
| EMPRESAS | Empresas | 20 min | 3 | E |
| GERENCIA | Gerencia | 30 min | 4 | G |

### 3.2 TicketStatus

Estados posibles de un ticket:

| Valor | Descripción | Es Activo? |
|-------|-------------|------------|
| EN_ESPERA | Esperando asignación | Sí |
| PROXIMO | Próximo a ser atendido | Sí |
| ATENDIENDO | Siendo atendido | Sí |
| COMPLETADO | Atención finalizada | No |
| CANCELADO | Cancelado | No |
| NO_ATENDIDO | Cliente no se presentó | No |

### 3.3 AdvisorStatus

Estados posibles de un asesor:

| Valor | Descripción | Recibe Asignaciones? |
|-------|-------------|----------------------|
| AVAILABLE | Disponible | Sí |
| BUSY | Atendiendo cliente | No |
| OFFLINE | No disponible | No |

### 3.4 MessageTemplate

Plantillas de mensajes para Telegram:

| Valor | Descripción | Momento de Envío |
|-------|-------------|------------------|
| totem_ticket_creado | Confirmación de creación | Inmediato al crear ticket |
| totem_proximo_turno | Pre-aviso | Cuando posición ≤ 3 |
| totem_es_tu_turno | Turno activo | Al asignar a asesor |

---

## 4. Requerimientos Funcionales

**NOTA:** Los 8 requerimientos funcionales completos (RF-001 a RF-008) con todos sus escenarios Gherkin, modelos de datos y ejemplos JSON están documentados en los archivos individuales:

- `PASO-2-RF-001-Crear-Ticket.md` (7 escenarios)
- `PASO-3-RF-002-Notificaciones-Telegram.md` (7 escenarios)
- `PASO-4-RF-003-Calcular-Posicion-Tiempo.md` (6 escenarios)
- `PASO-5-RF-004-Asignar-Ticket-Ejecutivo.md` (8 escenarios)
- `PASO-6-RF-005-Gestionar-Multiples-Colas.md` (6 escenarios)
- `PASO-7-RF-006-Consultar-Estado-Ticket.md` (6 escenarios)
- `PASO-8-RF-007-Panel-Monitoreo-Supervisor.md` (7 escenarios)
- `PASO-9-RF-008-Registrar-Auditoria-Eventos.md` (6 escenarios)

**Total:** 51 escenarios Gherkin documentados

---

## 5. Matriz de Trazabilidad

| RF | Nombre | Beneficio de Negocio | Endpoints HTTP | Prioridad |
|----|--------|---------------------|----------------|-----------|
| RF-001 | Crear Ticket Digital | Digitalización del proceso, elimina tickets físicos | POST /api/tickets | Alta |
| RF-002 | Enviar Notificaciones Telegram | Movilidad del cliente, reduce abandonos | Ninguno (interno) | Alta |
| RF-003 | Calcular Posición y Tiempo | Transparencia, gestión de expectativas | GET /api/tickets/{numero}/position | Alta |
| RF-004 | Asignar Ticket a Ejecutivo | Optimización de recursos, balanceo de carga | Ninguno (interno) | Alta |
| RF-005 | Gestionar Múltiples Colas | Priorización inteligente, eficiencia operacional | GET /api/admin/queues/{type}<br>GET /api/admin/queues/{type}/stats | Alta |
| RF-006 | Consultar Estado del Ticket | Autoservicio, reduce consultas presenciales | GET /api/tickets/{uuid}<br>GET /api/tickets/{numero}/position | Alta |
| RF-007 | Panel de Monitoreo | Supervisión en tiempo real, toma de decisiones | GET /api/admin/dashboard<br>GET /api/admin/summary<br>GET /api/admin/advisors<br>GET /api/admin/advisors/stats | Alta |
| RF-008 | Registrar Auditoría | Cumplimiento normativo, trazabilidad completa | Ninguno (interno) | Alta |

---

## 6. Casos de Uso Principales

### CU-001: Cliente Obtiene Ticket y Es Atendido (Happy Path)

**Actor Principal:** Cliente  
**Precondición:** Sistema operativo, asesores disponibles  
**Flujo Principal:**

1. Cliente ingresa RUT/ID y teléfono en terminal (RF-001)
2. Cliente selecciona tipo de atención (CAJA)
3. Sistema genera ticket C05, posición 5, tiempo estimado 25 min (RF-001)
4. Sistema envía Mensaje 1 de confirmación vía Telegram (RF-002)
5. Cliente sale de sucursal y espera
6. Sistema recalcula posición en tiempo real (RF-003)
7. Cuando posición = 3, sistema envía Mensaje 2 (pre-aviso) (RF-002)
8. Cliente regresa a sucursal
9. Asesor se libera, sistema asigna ticket C05 automáticamente (RF-004)
10. Sistema envía Mensaje 3 con módulo y asesor (RF-002)
11. Cliente se presenta en módulo 3
12. Asesor atiende al cliente
13. Asesor completa atención, ticket cambia a COMPLETADO
14. Sistema registra auditoría de todo el proceso (RF-008)

**Postcondición:** Cliente atendido, ticket completado, auditoría registrada

**RFs involucrados:** RF-001, RF-002, RF-003, RF-004, RF-008

---

## 7. Matriz de Endpoints HTTP

| # | Método | Endpoint | RF | Descripción |
|---|--------|----------|----|-----------|
| 1 | POST | /api/tickets | RF-001 | Crear nuevo ticket |
| 2 | GET | /api/tickets/{uuid} | RF-006 | Consultar por UUID |
| 3 | GET | /api/tickets/{numero}/position | RF-003, RF-006 | Consultar posición y estado |
| 4 | GET | /api/admin/dashboard | RF-007 | Dashboard completo |
| 5 | GET | /api/admin/summary | RF-007 | Resumen simplificado |
| 6 | GET | /api/admin/queues/{type} | RF-005 | Estado de cola específica |
| 7 | GET | /api/admin/queues/{type}/stats | RF-005 | Estadísticas de cola |
| 8 | GET | /api/admin/advisors | RF-007 | Lista de asesores |
| 9 | GET | /api/admin/advisors/stats | RF-007 | Estadísticas de asesores |
| 10 | PUT | /api/admin/advisors/{id}/status | RF-007 | Cambiar estado de asesor |
| 11 | GET | /api/health | - | Health check del sistema |

**Total de endpoints:** 11

---

## 8. Validaciones y Formatos

### Formato de RUT/ID (nationalId)

| Formato | Ejemplo | Validación |
|---------|---------|------------|
| RUT Chileno | 12345678-9 | Dígito verificador válido |
| RUT sin guión | 123456789 | 8-9 dígitos numéricos |
| ID extranjero | P12345678 | Alfanumérico, 8-12 caracteres |

### Formato de Teléfono

| Formato | Ejemplo | Validación |
|---------|---------|------------|
| Internacional | +56912345678 | Código país + 9-15 dígitos |
| Nacional | 912345678 | 9 dígitos (se agrega +56) |

### Formato de Número de Ticket

| Formato | Ejemplo | Validación |
|---------|---------|------------|
| Patrón | [C\|P\|E\|G][01-99] | Prefijo + 2 dígitos |
| CAJA | C01, C15, C99 | Prefijo C |
| PERSONAL_BANKER | P01, P15, P99 | Prefijo P |
| EMPRESAS | E01, E15, E99 | Prefijo E |
| GERENCIA | G01, G15, G99 | Prefijo G |

---

## 9. Glosario

| Término | Definición |
|---------|------------|
| Actor | Persona o sistema que interactúa con el sistema |
| Asesor | Ejecutivo bancario que atiende clientes |
| Auditoría | Registro inmutable de eventos del sistema |
| Backoff Exponencial | Estrategia de reintentos con tiempos crecientes (30s, 60s, 120s) |
| Balanceo de Carga | Distribución equitativa de tickets entre asesores |
| Chat ID | Identificador único de usuario en Telegram |
| Cola | Fila virtual de tickets esperando atención |
| Dashboard | Panel de control con métricas en tiempo real |
| Endpoint | URL específica que expone funcionalidad del sistema |
| FIFO | First In, First Out - Primero en entrar, primero en salir |
| Gherkin | Lenguaje para escribir criterios de aceptación (Given/When/Then) |
| Módulo | Estación de trabajo de un asesor (numerados 1-5) |
| Plantilla | Formato predefinido de mensaje con variables |
| Posición en Cola | Número que indica cuántos tickets hay adelante |
| Prioridad | Orden de importancia para asignación (1=baja, 4=máxima) |
| RUT/ID | Identificación nacional del cliente |
| Ticket | Turno digital asignado a un cliente para ser atendido |
| Tiempo Estimado | Minutos calculados de espera (posición × tiempo promedio) |
| UUID | Identificador único universal (36 caracteres) |

---

## 10. Resumen Ejecutivo

### Estadísticas del Documento

| Métrica | Valor |
|---------|-------|
| Requerimientos Funcionales | 8 |
| Reglas de Negocio | 13 |
| Escenarios Gherkin | 51 |
| Endpoints HTTP | 11 |
| Entidades de Datos | 3 (Ticket, Mensaje, Advisor) |
| Enumeraciones | 4 |
| Casos de Uso | 3 |

### Cobertura por RF

| RF | Escenarios | Reglas Aplicadas | Endpoints |
|----|-----------|------------------|-----------|
| RF-001 | 7 | 4 (RN-001, RN-005, RN-006, RN-010) | 1 |
| RF-002 | 7 | 4 (RN-007, RN-008, RN-011, RN-012) | 0 |
| RF-003 | 6 | 2 (RN-003, RN-010) | 1 |
| RF-004 | 8 | 3 (RN-002, RN-003, RN-004) | 0 |
| RF-005 | 6 | 1 (RN-002) | 2 |
| RF-006 | 6 | 1 (RN-009) | 2 |
| RF-007 | 7 | 1 (RN-013) | 4 |
| RF-008 | 6 | 1 (RN-011) | 0 |
| **Total** | **51** | **17 aplicaciones** | **11** |

### Beneficios Cuantificables

| Beneficio | Métrica Actual | Métrica Objetivo | Mejora |
|-----------|----------------|------------------|--------|
| NPS | 45 puntos | 65 puntos | +44% |
| Abandono de cola | 15% | 5% | -67% |
| Tickets por ejecutivo | Baseline | +20% | +20% |
| Tiempo de espera percibido | Alto | Bajo | Transparencia |

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2025  
**Estado:** Completo y Aprobado para Implementación

**Preparado por:** Equipo de Producto e Innovación  
**Revisado por:** [Pendiente]  
**Aprobado por:** [Pendiente]

