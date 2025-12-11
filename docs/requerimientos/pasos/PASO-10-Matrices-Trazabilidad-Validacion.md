# PASO 10: Matrices de Trazabilidad y Validación Final

## Contenido de este paso:
- ✅ Matriz de Trazabilidad RF → Beneficio → Endpoints
- ✅ Matriz de Dependencias entre RFs
- ✅ Matriz de Endpoints HTTP (11 endpoints)
- ✅ Casos de Uso Principales (3 casos)
- ✅ Validaciones y Reglas de Formato
- ✅ Checklist de Validación Final
- ✅ Glosario de Términos

---

## 1. Matriz de Trazabilidad RF → Beneficio → Endpoints

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

## 2. Matriz de Dependencias entre RFs

| RF Origen | Depende de | Tipo de Dependencia | Descripción |
|-----------|------------|---------------------|-------------|
| RF-002 | RF-001 | Fuerte | Notificaciones requieren ticket creado |
| RF-003 | RF-001 | Fuerte | Cálculo requiere tickets en cola |
| RF-004 | RF-001 | Fuerte | Asignación requiere tickets en espera |
| RF-004 | RF-005 | Media | Asignación usa prioridades de colas |
| RF-006 | RF-001 | Fuerte | Consulta requiere ticket existente |
| RF-007 | RF-001, RF-004, RF-005 | Media | Dashboard agrega datos de tickets, asesores y colas |
| RF-008 | Todos | Débil | Auditoría registra eventos de todos los RFs |

**Leyenda:**
- **Fuerte:** RF no puede funcionar sin la dependencia
- **Media:** RF funciona parcialmente sin la dependencia
- **Débil:** RF es independiente pero se relaciona

---

## 3. Matriz de Endpoints HTTP

| # | Método | Endpoint | RF | Descripción | Request Body | Response |
|---|--------|----------|----|-----------|--------------| ---------|
| 1 | POST | /api/tickets | RF-001 | Crear nuevo ticket | ✅ | 201, 409, 400 |
| 2 | GET | /api/tickets/{uuid} | RF-006 | Consultar por UUID | ❌ | 200, 404, 400 |
| 3 | GET | /api/tickets/{numero}/position | RF-003, RF-006 | Consultar posición y estado | ❌ | 200, 404 |
| 4 | GET | /api/admin/dashboard | RF-007 | Dashboard completo | ❌ | 200 |
| 5 | GET | /api/admin/summary | RF-007 | Resumen simplificado | ❌ | 200 |
| 6 | GET | /api/admin/queues/{type} | RF-005 | Estado de cola específica | ❌ | 200, 400 |
| 7 | GET | /api/admin/queues/{type}/stats | RF-005 | Estadísticas de cola | ❌ | 200, 400 |
| 8 | GET | /api/admin/advisors | RF-007 | Lista de asesores | ❌ | 200 |
| 9 | GET | /api/admin/advisors/stats | RF-007 | Estadísticas de asesores | ❌ | 200 |
| 10 | PUT | /api/admin/advisors/{id}/status | RF-007 | Cambiar estado de asesor | ✅ | 200, 404 |
| 11 | GET | /api/health | - | Health check del sistema | ❌ | 200 |

**Total de endpoints:** 11

---

## 4. Casos de Uso Principales

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

### CU-002: Supervisor Monitorea Operación en Tiempo Real

**Actor Principal:** Supervisor  
**Precondición:** Usuario autenticado con rol supervisor  
**Flujo Principal:**

1. Supervisor accede al dashboard (RF-007)
2. Sistema muestra resumen de tickets por estado
3. Sistema muestra clientes en espera por cola (RF-005)
4. Sistema muestra estado de asesores (RF-007)
5. Sistema detecta cola CAJA con 18 tickets (> 15)
6. Sistema genera alerta "COLA_CRITICA" (RF-007)
7. Supervisor visualiza alerta en dashboard
8. Dashboard se actualiza automáticamente cada 5 segundos
9. Supervisor toma decisión operacional (asignar más asesores a CAJA)

**Postcondición:** Supervisor informado, decisión tomada

**RFs involucrados:** RF-005, RF-007

---

### CU-003: Cliente Consulta Estado de Su Ticket

**Actor Principal:** Cliente  
**Precondición:** Cliente tiene ticket activo  
**Flujo Principal:**

1. Cliente abre link de consulta con UUID
2. Cliente consulta GET /api/tickets/{uuid} (RF-006)
3. Sistema retorna estado actual: EN_ESPERA
4. Sistema retorna posición: 3
5. Sistema retorna tiempo estimado: 15 minutos
6. Cliente ve que está próximo (posición ≤ 3)
7. Cliente decide acercarse a sucursal

**Postcondición:** Cliente informado de su estado

**RFs involucrados:** RF-006

---

## 5. Validaciones y Reglas de Formato

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

### Formato de UUID

| Formato | Ejemplo | Validación |
|---------|---------|------------|
| UUID v4 | a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6 | 36 caracteres con guiones |

---

## 6. Checklist de Validación Final

### Completitud

- [x] 8 Requerimientos Funcionales documentados (RF-001 a RF-008)
- [x] 13 Reglas de Negocio numeradas (RN-001 a RN-013)
- [x] 4 Enumeraciones definidas (QueueType, TicketStatus, AdvisorStatus, MessageTemplate)
- [x] 3 Entidades principales (Ticket, Mensaje, Advisor)
- [x] 11 Endpoints HTTP mapeados
- [x] 51 Escenarios Gherkin totales (RF-001:7, RF-002:7, RF-003:6, RF-004:8, RF-005:6, RF-006:6, RF-007:7, RF-008:6)
- [x] 3 Casos de Uso principales documentados
- [x] Matriz de trazabilidad completa
- [x] Matriz de dependencias entre RFs

### Calidad

- [x] Formato Gherkin correcto (Given/When/Then/And)
- [x] Ejemplos JSON válidos en respuestas HTTP
- [x] Sin ambigüedades en descripciones
- [x] Sin mencionar tecnologías de implementación
- [x] Numeración consistente (RF-XXX, RN-XXX)
- [x] Tablas bien formateadas
- [x] Jerarquía clara con encabezados

### Trazabilidad

- [x] Cada RF tiene reglas de negocio aplicables
- [x] Cada RF tiene criterios de aceptación verificables
- [x] Cada endpoint mapeado a RF correspondiente
- [x] Cada regla de negocio aplicada en al menos un RF
- [x] Dependencias entre RFs identificadas

---

## 7. Glosario de Términos

| Término | Definición |
|---------|------------|
| Actor | Persona o sistema que interactúa con el sistema |
| Asesor | Ejecutivo bancario que atiende clientes |
| Auditoría | Registro inmutable de eventos del sistema |
| Backoff Exponencial | Estrategia de reintentos con tiempos crecientes (30s, 60s, 120s) |
| Balanceo de Carga | Distribución equitativa de tickets entre asesores |
| Chat ID | Identificador único de usuario en Telegram |
| Cola | Fila virtual de tickets esperando atención |
| Criterio de Aceptación | Condición verificable que debe cumplir un requerimiento |
| Dashboard | Panel de control con métricas en tiempo real |
| Endpoint | URL específica que expone funcionalidad del sistema |
| FIFO | First In, First Out - Primero en entrar, primero en salir |
| Gherkin | Lenguaje para escribir criterios de aceptación (Given/When/Then) |
| Módulo | Estación de trabajo de un asesor (numerados 1-5) |
| Plantilla | Formato predefinido de mensaje con variables |
| Posición en Cola | Número que indica cuántos tickets hay adelante |
| Prioridad | Orden de importancia para asignación (1=baja, 4=máxima) |
| RUT/ID | Identificación nacional del cliente (Rol Único Tributario o ID) |
| Ticket | Turno digital asignado a un cliente para ser atendido |
| Tiempo Estimado | Minutos calculados de espera (posición × tiempo promedio) |
| UUID | Identificador único universal (36 caracteres) |

---

## 8. Resumen Ejecutivo del Documento

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
| Páginas Estimadas | 60-70 |

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

## 9. Próximos Pasos

### Para Validación

1. **Revisión por Stakeholders:** Product Owner, Arquitecto, Líder Técnico
2. **Validación de Negocio:** Gerente de Sucursal, Supervisor de Atención
3. **Aprobación Formal:** Firma de documento por stakeholders clave

### Para Implementación

1. **PROMPT 2:** Diseño de Arquitectura (basado en este documento)
2. **PROMPT 3:** Implementación de Backend
3. **PROMPT 4:** Implementación de Frontend
4. **PROMPT 5:** Testing y QA

### Para Mantenimiento

1. **Versionamiento:** Este documento es versión 1.0
2. **Actualizaciones:** Cambios deben ser aprobados por Product Owner
3. **Trazabilidad:** Mantener sincronización con código implementado

---

## Resumen del PASO 10

**Elementos documentados:**
- ✅ Matriz de Trazabilidad RF → Beneficio → Endpoints (8 RFs)
- ✅ Matriz de Dependencias entre RFs (7 dependencias)
- ✅ Matriz de Endpoints HTTP (11 endpoints)
- ✅ 3 Casos de Uso Principales
- ✅ Validaciones y Reglas de Formato (4 formatos)
- ✅ Checklist de Validación Final (3 categorías)
- ✅ Glosario de Términos (20 términos)
- ✅ Resumen Ejecutivo con estadísticas

**Validación Final:**
- ✅ 8 RFs documentados completamente
- ✅ 51 escenarios Gherkin totales
- ✅ 13 reglas de negocio aplicadas
- ✅ 11 endpoints HTTP mapeados
- ✅ 3 casos de uso end-to-end
- ✅ Trazabilidad completa
- ✅ Sin ambigüedades
- ✅ Formato profesional

