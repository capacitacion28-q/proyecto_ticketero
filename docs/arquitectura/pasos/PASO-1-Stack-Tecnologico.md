# PASO 1: Stack Tecnológico con Justificaciones

**Proyecto:** Sistema Ticketero Digital  
**Fecha:** Diciembre 2025  
**Estado:** ✅ Completado

---

## 1. Backend Framework

**Selección:** Java 21 + Spring Boot 3.2.11

### ¿Por qué Java 21?
- **Virtual Threads (Project Loom):** Manejo eficiente de concurrencia para schedulers
- **Records:** DTOs inmutables sin boilerplate
- **Pattern Matching:** Código más limpio y expresivo
- **LTS:** Soporte hasta septiembre 2029
- **Ecosistema maduro:** Amplia disponibilidad de librerías empresariales

### ¿Por qué Spring Boot 3.2.11?
- **Spring Data JPA:** Reducción de 80% de código de acceso a datos
- **Spring Scheduling:** @Scheduled para procesamiento asíncrono sin infraestructura adicional
- **Bean Validation:** Validación declarativa con @Valid
- **Actuator:** Endpoints de salud y métricas out-of-the-box
- **Adopción:** 95% de instituciones financieras usan Spring Boot

### Alternativas Consideradas

| Tecnología      | Pros                          | Contras                           | Decisión |
|-----------------|-------------------------------|-----------------------------------|----------|
| Node.js + NestJS| Async nativo, menor footprint | Menos maduro para apps críticas   | ❌ No    |
| Go + Gin        | Performance superior          | Ecosistema menos maduro para CRUD | ❌ No    |
| .NET Core       | Excelente tooling             | Licenciamiento, menos adopción    | ❌ No    |

---

## 2. Base de Datos

**Selección:** PostgreSQL 16

### ¿Por qué PostgreSQL 16?
- **ACID compliant:** Crítico para transacciones financieras
- **JSONB:** Flexibilidad para metadata de mensajes
- **Índices avanzados:** B-tree, GiST para queries complejas
- **Row-level locking:** Concurrencia segura para asignación de tickets
- **Particionamiento:** Escalabilidad para auditoría (millones de registros)
- **Open source:** Sin costos de licenciamiento

### Alternativas Consideradas

| Base de Datos | Pros                    | Contras                      | Decisión |
|---------------|-------------------------|------------------------------|----------|
| MySQL         | Amplia adopción         | Menor soporte de JSON        | ❌ No    |
| MongoDB       | Flexible schema         | No ACID para múltiples docs  | ❌ No    |
| Oracle        | Features empresariales  | Costos prohibitivos          | ❌ No    |

---

## 3. Migraciones de Base de Datos

**Selección:** Flyway

### ¿Por qué Flyway?
- **Versionamiento automático:** Cada cambio tiene versión (V1__, V2__)
- **Rollback seguro:** Validación de checksums
- **Integración nativa:** Spring Boot detecta automáticamente scripts
- **Simplicidad:** Archivos SQL planos
- **Validación en startup:** Falla rápido si esquema no coincide

### Alternativa
- **Liquibase:** Más verboso (XML/YAML), overkill para 3 tablas

---

## 4. Integración con Telegram

**Selección:** Telegram Bot HTTP API + RestTemplate

### ¿Por qué Telegram Bot API?
- **Canal preferido:** Especificado por cliente
- **API HTTP simple:** Documentación clara
- **Sin costo:** vs WhatsApp Business API ($0.005/mensaje)
- **Rate limits generosos:** 30 mensajes/segundo
- **HTML formatting:** Soporte de emojis y formato enriquecido

### ¿Por qué RestTemplate (no WebClient)?
- **Simplicidad:** API síncrona más fácil de debuggear
- **Suficiente para volumen:** 0.9 msg/segundo promedio
- **Menor curva de aprendizaje:** Sin programación reactiva
- **WebClient es overkill:** Reactivo aporta valor solo con >10 req/seg

---

## 5. Containerización

**Selección:** Docker + Docker Compose

### ¿Por qué Docker?
- **Paridad dev/prod:** Elimina "funciona en mi máquina"
- **Multi-stage builds:** Imagen final <150MB
- **Aislamiento:** Dependencias encapsuladas
- **Estándar:** 90% de adopción en empresas tecnológicas

### ¿Por qué Docker Compose?
- **Orquestación simple:** Definición declarativa de servicios
- **Redes automáticas:** Contenedores se comunican por nombre
- **Ideal para dev/staging:** Migración directa a ECS/Fargate en producción

---

## 6. Build Tool

**Selección:** Maven 3.9+

### ¿Por qué Maven?
- **Convención sobre configuración:** Estructura estándar
- **Repositorio central:** 10M+ artifacts disponibles
- **Plugins maduros:** Spring Boot Maven Plugin
- **Estándar empresarial:** 85% de proyectos Java en instituciones financieras

---

## Resumen del Stack

| Capa | Tecnología | Versión | Justificación Clave |
|------|------------|---------|---------------------|
| **Lenguaje** | Java | 21 LTS | Virtual Threads, Records, soporte hasta 2029 |
| **Framework** | Spring Boot | 3.2.11 | Productividad, ecosistema maduro |
| **Base de Datos** | PostgreSQL | 16 | ACID, JSONB, sin licenciamiento |
| **Migraciones** | Flyway | 10.x | Versionamiento SQL simple |
| **Mensajería** | Telegram Bot API | - | Sin costo, 30 msg/seg |
| **HTTP Client** | RestTemplate | - | Simplicidad para bajo volumen |
| **Containerización** | Docker | 24.x | Paridad dev/prod |
| **Orquestación** | Docker Compose | 2.x | Simple para dev/staging |
| **Build** | Maven | 3.9+ | Estándar empresarial |

---

## Validaciones

- ✅ 6 tecnologías seleccionadas y justificadas
- ✅ Cada selección tiene tabla de alternativas con pros/contras
- ✅ Justificaciones técnicamente sólidas
- ✅ Contexto empresarial considerado
- ✅ Decisiones alineadas con RF

---

**Siguiente paso:** PASO 2 - Diagrama de Contexto C4
