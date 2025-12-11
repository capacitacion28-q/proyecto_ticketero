# PASO 2: Diagrama de Contexto C4

**Proyecto:** Sistema Ticketero Digital  
**Fecha:** Diciembre 2025  
**Estado:** ✅ Completado

---

## Diagrama de Contexto C4 (Level 1)

El siguiente diagrama muestra el sistema Ticketero en su contexto, incluyendo actores externos y sistemas con los que interactúa.

```plantuml
@startuml Diagrama de Contexto - Sistema Ticketero
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Context.puml

title Diagrama de Contexto (C4 Level 1) - Sistema Ticketero

' Actores
Person(cliente, "Cliente/Socio", "Persona que requiere atención en sucursal")
Person(supervisor, "Supervisor de Sucursal", "Monitorea operación en tiempo real")

' Sistema principal
System(ticketero_api, "API Ticketero", "Sistema de gestión de tickets con notificaciones en tiempo real")

' Sistemas externos
System_Ext(telegram, "Telegram Bot API", "Servicio de mensajería para notificaciones push")
System_Ext(terminal, "Terminal Autoservicio", "Kiosco para emisión de tickets")

' Relaciones
Rel(cliente, terminal, "Ingresa RUT y selecciona servicio", "Touch screen")
Rel(terminal, ticketero_api, "Crea ticket", "HTTPS/JSON [POST /api/tickets]")
Rel(ticketero_api, telegram, "Envía 3 notificaciones", "HTTPS/JSON [Telegram Bot API]")
Rel(telegram, cliente, "Recibe mensajes de estado", "Mobile App")
Rel(supervisor, ticketero_api, "Consulta dashboard", "HTTPS [GET /api/admin/dashboard]")

SHOW_LEGEND()

@enduml
```

**Archivo fuente:** [01-context-diagram.puml](../diagrams/01-context-diagram.puml)

**Para visualizar:** Usar plugins de PlantUML en IDE o http://www.plantuml.com/plantuml/

---

## Elementos del Diagrama

### Actores (Person)
- **Cliente/Socio:** Persona que requiere atención en sucursal
- **Supervisor de Sucursal:** Monitorea operación en tiempo real

### Sistema Principal
- **API Ticketero:** Sistema de gestión de tickets con notificaciones en tiempo real

### Sistemas Externos (System_Ext)
- **Telegram Bot API:** Servicio de mensajería para notificaciones push
- **Terminal Autoservicio:** Kiosco para emisión de tickets

### Relaciones (Rel)
1. **Cliente → Terminal:** Ingresa RUT y selecciona servicio (Touch screen)
2. **Terminal → API Ticketero:** Crea ticket (HTTPS/JSON [POST /api/tickets])
3. **API Ticketero → Telegram:** Envía 3 notificaciones (HTTPS/JSON [Telegram Bot API])
4. **Telegram → Cliente:** Recibe mensajes de estado (Mobile App)
5. **Supervisor → API Ticketero:** Consulta dashboard (HTTPS [GET /api/admin/dashboard])

---

## Flujos Principales

### Flujo de Creación de Ticket
1. Cliente interactúa con Terminal Autoservicio
2. Terminal envía request HTTP a API Ticketero
3. API procesa y retorna ticket creado
4. API programa mensajes para envío vía Telegram

### Flujo de Notificaciones
1. API Ticketero envía mensajes a Telegram Bot API
2. Telegram entrega notificaciones al cliente en su móvil
3. Cliente recibe 3 tipos de mensajes: confirmación, pre-aviso, turno activo

### Flujo de Supervisión
1. Supervisor accede al dashboard de la API
2. API retorna métricas en tiempo real
3. Dashboard se actualiza cada 5 segundos

---

## Protocolos y Tecnologías

| Conexión | Protocolo | Formato | Ejemplo |
|----------|-----------|---------|---------|
| Terminal → API | HTTPS | JSON | POST /api/tickets |
| API → Telegram | HTTPS | JSON | POST /bot{token}/sendMessage |
| Supervisor → API | HTTPS | JSON | GET /api/admin/dashboard |
| Telegram → Cliente | Push | Nativo | Notificación móvil |

---

## Validaciones

- ✅ Diagrama PlantUML válido y renderizable
- ✅ 2 actores identificados (Cliente, Supervisor)
- ✅ 1 sistema principal (Ticketero API)
- ✅ 2 sistemas externos (Telegram, Terminal)
- ✅ 5 relaciones documentadas con protocolos
- ✅ Leyenda incluida (SHOW_LEGEND())
- ✅ Archivo fuente creado en diagrams/

---

**Siguiente paso:** PASO 3 - Diagrama de Secuencia