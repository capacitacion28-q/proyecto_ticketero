# PASO 6: Decisiones Arquitectónicas (ADRs)

**Proyecto:** Sistema Ticketero Digital  
**Fecha:** Diciembre 2025  
**Estado:** ✅ Completado

---

## ADR-001: No usar Circuit Breakers (Resilience4j)

### Contexto
Telegram Bot API es un servicio externo que podría fallar, y se consideró implementar Circuit Breaker para proteger el sistema.

### Decisión
**NO implementar Circuit Breaker** en esta fase del proyecto.

### Razones
- **Simplicidad 80/20:** Circuit Breaker agrega complejidad innecesaria para el volumen actual
- **Volumen bajo:** 25,000 mensajes/día = 0.3 msg/segundo promedio (no crítico)
- **Telegram confiable:** 99.9% uptime según SLA público
- **Reintentos suficientes:** RN-007 y RN-008 cubren fallos temporales
- **Degradación aceptable:** Si Telegram falla, mensajes quedan PENDIENTES y se reintenta automáticamente

### Consecuencias
- ✅ **Positivas:**
  - Código más simple y mantenible
  - Menor curva de aprendizaje para el equipo
  - Menos dependencias externas
- ❌ **Negativas:**
  - Sin protección contra cascading failures (aceptable para este volumen)
  - No hay fallback automático a otros canales

### Revisión Futura
- **Fase 2 (50+ sucursales):** Reevaluar Resilience4j cuando volumen supere 10 msg/segundo

---

## ADR-002: RestTemplate en lugar de WebClient

### Contexto
Spring Boot 3 recomienda WebClient (reactivo) sobre RestTemplate (blocking) para HTTP clients.

### Decisión
Usar **RestTemplate** para integración con Telegram Bot API.

### Razones
- **Simplicidad:** API síncrona más fácil de debuggear y mantener
- **Volumen apropiado:** 0.3 requests/segundo a Telegram no requiere programación reactiva
- **Curva de aprendizaje:** WebClient requiere conocimiento de Project Reactor
- **Stack trace claro:** Debugging más simple con blocking I/O
- **Suficiente performance:** Para este volumen, blocking I/O es adecuado

### Consecuencias
- ✅ **Positivas:**
  - Código más simple y legible
  - Stack traces más fáciles de interpretar
  - Menor complejidad cognitiva
- ❌ **Negativas:**
  - Menor throughput teórico (no relevante para este caso)
  - RestTemplate está en "maintenance mode"

### Revisión Futura
- **Si volumen supera 10 req/segundo:** Migrar a WebClient
- **Fase Nacional:** Evaluar programación reactiva completa

---

## ADR-003: Scheduler en lugar de Queue (RabbitMQ/Kafka)

### Contexto
Los mensajes deben enviarse en tiempos específicos (inmediato, cuando posición ≤3, al asignar). Se consideró usar message queues vs schedulers.

### Decisión
Usar **@Scheduled + tabla mensaje** en PostgreSQL como "queue".

### Razones
- **Simplicidad infraestructural:** No requiere RabbitMQ/Kafka adicional
- **Volumen apropiado:** 75,000 mensajes/día = 0.9 msg/segundo
- **Polling aceptable:** @Scheduled cada 60s es suficiente para este throughput
- **PostgreSQL confiable:** ACID como garantía de entrega
- **Menos moving parts:** Solo PostgreSQL + API, sin brokers externos

### Consecuencias
- ✅ **Positivas:**
  - Infraestructura simple (solo PostgreSQL + API)
  - Sin complejidad de message brokers
  - Transacciones ACID para mensajes
- ❌ **Negativas:**
  - Polling cada 60s (no tiempo real extremo, pero aceptable)
  - No hay distribución automática de carga entre instancias

### Revisión Futura
- **Fase Nacional (500,000+ mensajes/día):** Migrar a RabbitMQ o Kafka
- **Múltiples instancias:** Implementar distributed locking

---

## ADR-004: Flyway para Migraciones

### Contexto
Se necesita versionamiento de esquema de base de datos para despliegues controlados.

### Decisión
Usar **Flyway** en lugar de Liquibase o migraciones manuales.

### Razones
- **SQL nativo:** Archivos SQL planos, fáciles de leer y mantener
- **Versionamiento automático:** V1__, V2__ con checksums
- **Integración Spring Boot:** Detección automática en classpath
- **Rollback seguro:** Validación de integridad en startup
- **Simplicidad:** Sin XML/YAML verboso como Liquibase

### Consecuencias
- ✅ **Positivas:**
  - Esquema versionado y auditable
  - Despliegues reproducibles
  - Fácil de entender para DBAs
- ❌ **Negativas:**
  - Menos features que Liquibase (no necesarios para este proyecto)

### Revisión Futura
- **Estable:** No se prevén cambios, Flyway es suficiente para el ciclo de vida del proyecto

---

## ADR-005: Bean Validation (@Valid) en DTOs

### Contexto
Se necesita validar requests HTTP de manera consistente y declarativa.

### Decisión
Usar **Bean Validation** con anotaciones @Valid en DTOs.

### Razones
- **Declarativo:** @NotBlank, @Pattern directamente en DTOs
- **Automático:** Spring valida automáticamente con @Valid
- **Mensajes estandarizados:** Formato consistente de errores
- **Separación de concerns:** Validación separada de lógica de negocio
- **Reutilizable:** Mismas validaciones en múltiples endpoints

### Ejemplo
```java
public record TicketRequest(
    @NotBlank(message = "RUT/ID es obligatorio") 
    String nationalId,
    
    @Pattern(regexp = "^\\+56[0-9]{9}$", message = "Formato de teléfono inválido") 
    String telefono,
    
    @NotNull(message = "Tipo de cola es obligatorio") 
    QueueType queueType
) {}
```

### Consecuencias
- ✅ **Positivas:**
  - Validación consistente y declarativa
  - Mensajes de error estandarizados
  - Código más limpio en controllers
- ❌ **Negativas:**
  - Validaciones complejas requieren custom validators

### Revisión Futura
- **Estable:** Bean Validation es estándar de la industria

---

## Resumen de Decisiones

| ADR | Decisión | Alternativa Rechazada | Razón Principal |
|-----|----------|----------------------|-----------------|
| ADR-001 | Sin Circuit Breaker | Resilience4j | Simplicidad para bajo volumen |
| ADR-002 | RestTemplate | WebClient | Simplicidad sobre performance |
| ADR-003 | @Scheduled + PostgreSQL | RabbitMQ/Kafka | Menos infraestructura |
| ADR-004 | Flyway | Liquibase | SQL nativo más simple |
| ADR-005 | Bean Validation | Validación manual | Declarativo y consistente |

---

## Principios Aplicados

### Simplicidad sobre Complejidad
- Todas las decisiones priorizan simplicidad para el volumen actual
- Evitar over-engineering prematuro
- Principio 80/20: máximo valor con mínima complejidad

### Decisiones Reversibles
- Todas las decisiones pueden revertirse en fases futuras
- No hay vendor lock-in crítico
- Migración gradual posible

### Contexto Específico
- Decisiones basadas en volumen real (25K tickets/día)
- Consideración del equipo de desarrollo
- Infraestructura disponible

---

## Validaciones

- ✅ 5 ADRs documentados con formato estándar
- ✅ Cada ADR tiene: Contexto, Decisión, Razones, Consecuencias
- ✅ Alternativas consideradas y rechazadas
- ✅ Revisión futura especificada
- ✅ Principios arquitectónicos aplicados
- ✅ Tabla resumen incluida

---

**Siguiente paso:** PASO 7 - Configuración y Deployment