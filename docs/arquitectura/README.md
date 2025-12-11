# Arquitectura de Software - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Arquitecto:** Amazon Q Developer

---

## Estructura de Documentación

```
docs/arquitectura/
├── README.md                          # Este archivo (índice principal)
├── pasos/                             # Documentos por paso
│   ├── PASO-1-Stack-Tecnologico.md    # ✅ Completado
│   ├── PASO-2-Diagrama-C4.md          # ⏳ Pendiente
│   ├── PASO-3-Diagrama-Secuencia.md   # ⏳ Pendiente
│   ├── PASO-4-Modelo-Datos-ER.md      # ⏳ Pendiente
│   ├── PASO-5-Arquitectura-Capas.md   # ⏳ Pendiente
│   ├── PASO-6-ADRs.md                 # ⏳ Pendiente
│   └── PASO-7-Configuracion.md        # ⏳ Pendiente
└── diagrams/                          # Diagramas PlantUML
    ├── 01-context-diagram.puml        # ⏳ Pendiente
    ├── 02-sequence-diagram.puml       # ⏳ Pendiente
    └── 03-er-diagram.puml             # ⏳ Pendiente
```

---

## Resumen Ejecutivo

Sistema Ticketero Digital diseñado para modernizar la experiencia de atención en sucursales bancarias mediante:
- Digitalización completa del proceso de tickets
- Notificaciones automáticas en tiempo real vía Telegram
- Asignación inteligente de clientes a ejecutivos

**Características Principales:**
- API REST con Java 21 + Spring Boot 3.2.11
- Base de datos PostgreSQL 16 (3 tablas)
- Integración con Telegram Bot API
- Procesamiento asíncrono con schedulers
- Arquitectura en capas
- Containerización con Docker

**Capacidad:**
- Fase Piloto: 500-800 tickets/día (1 sucursal)
- Fase Expansión: 2,500-3,000 tickets/día (5 sucursales)
- Fase Nacional: 25,000+ tickets/día (50+ sucursales)

---

## Stack Tecnológico

| Capa | Tecnología | Versión | Justificación |
|------|------------|---------|---------------|
| Lenguaje | Java | 21 LTS | Virtual Threads, Records |
| Framework | Spring Boot | 3.2.11 | Productividad, ecosistema maduro |
| Base de Datos | PostgreSQL | 16 | ACID, JSONB |
| Migraciones | Flyway | 10.x | Versionamiento SQL |
| Mensajería | Telegram Bot API | - | Sin costo, 30 msg/seg |
| HTTP Client | RestTemplate | - | Simplicidad |
| Containerización | Docker | 24.x | Paridad dev/prod |
| Orquestación | Docker Compose | 2.x | Simple para dev/staging |
| Build | Maven | 3.9+ | Estándar empresarial |

Ver detalles completos en: [PASO-1-Stack-Tecnologico.md](pasos/PASO-1-Stack-Tecnologico.md)

---

## Progreso de Documentación

### ✅ PASO 1: Stack Tecnológico
- 6 tecnologías seleccionadas y justificadas
- Tablas de alternativas con pros/contras
- Decisiones alineadas con requerimientos

### ✅ PASO 2: Diagrama de Contexto C4
- Diagrama PlantUML renderizable
- 2 actores, 1 sistema principal, 2 sistemas externos
- 5 relaciones documentadas con protocolos

### ✅ PASO 3: Diagrama de Secuencia
- Flujo end-to-end completo (5 fases)
- 9 participantes documentados
- ~30 interacciones entre componentes

### ✅ PASO 4: Modelo de Datos ER
- 3 tablas principales (ticket, mensaje, advisor)
- 2 relaciones (1:N) documentadas
- Índices y constraints especificados

### ✅ PASO 5: Arquitectura en Capas
- 5 capas documentadas con responsabilidades
- 9 componentes principales especificados
- Dependencias y reglas de arquitectura definidas

### ✅ PASO 6: Decisiones Arquitectónicas (ADRs)
- 5 ADRs documentados con formato estándar
- Contexto, decisión, razones y consecuencias
- Principios de simplicidad aplicados

### ✅ PASO 7: Configuración y Deployment
- Variables de entorno documentadas
- Docker Compose y Dockerfile listos
- Application properties con profiles
- Scripts de deployment incluidos

---

## Principios de Diseño

- **Simplicidad sobre complejidad** (Regla 80/20)
- **Código como documentación**
- **Decisiones reversibles**
- **Escalabilidad progresiva**

---

## Referencias

- [Requerimientos Funcionales](../requerimientos/REQUERIMIENTOS-FUNCIONALES-COMPLETO.md)
- [Contexto del Proyecto](../project-requirements.md)
- [Regla de Simplicidad Verificable](../../.amazonq/rules/SimplicidadVerificable.md)

---

**Estado:** ✅ Completado (7/7 pasos)  
**Última actualización:** Diciembre 2025

---

## 🎉 DOCUMENTACIÓN COMPLETA

- **11 archivos generados** (7 pasos + 3 diagramas + README)
- **3 diagramas PlantUML** renderizables
- **9 componentes arquitectónicos** documentados
- **5 ADRs** con decisiones justificadas
- **Configuración completa** para desarrollo y producción

**Listo para:** Revisión técnica e implementación
