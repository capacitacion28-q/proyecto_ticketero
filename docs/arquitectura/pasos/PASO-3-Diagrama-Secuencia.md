# PASO 3: Diagrama de Secuencia del Flujo Completo

**Proyecto:** Sistema Ticketero Digital  
**Fecha:** Diciembre 2025  
**Estado:** ✅ Completado

---

## Diagrama de Secuencia End-to-End

El siguiente diagrama muestra el flujo completo desde la creación del ticket hasta la atención completada.

```plantuml
@startuml Secuencia Completa - Sistema Ticketero

title Flujo End-to-End: Creación de Ticket y Asignación a Asesor

actor Cliente
participant "Terminal" as Terminal
participant "TicketController" as Controller
participant "TicketService" as Service
participant "TelegramService" as Telegram
participant "PostgreSQL" as DB
participant "MessageScheduler" as Scheduler
participant "QueueProcessor" as QueueProc
participant "Advisor" as Asesor

== Fase 1: Creación de Ticket ==

Cliente -> Terminal: Ingresa RUT, teléfono, selecciona PERSONAL_BANKER
Terminal -> Controller: POST /api/tickets
Controller -> Service: crearTicket(request)
Service -> DB: INSERT INTO ticket (numero: P01, status: EN_ESPERA, positionInQueue: 5)
Service -> DB: INSERT INTO mensaje (3 mensajes programados)
Service --> Controller: TicketResponse(numero: P01, positionInQueue: 5, estimatedWait: 75min)
Controller --> Terminal: HTTP 201 + JSON
Terminal --> Cliente: Muestra ticket P01

== Fase 2: Envío de Mensaje 1 (Inmediato) ==

note over Scheduler: Ejecuta cada 60 segundos
Scheduler -> DB: SELECT mensajes WHERE estadoEnvio=PENDIENTE AND fechaProgramada <= NOW
DB --> Scheduler: [Mensaje 1: totem_ticket_creado]
Scheduler -> Telegram: POST sendMessage (chatId, "✅ Ticket P01, posición #5, 75min")
Telegram --> Scheduler: {ok: true, message_id: 123}
Scheduler -> DB: UPDATE mensaje SET estadoEnvio=ENVIADO, telegramMessageId=123
Scheduler --> Cliente: Notificación en Telegram

== Fase 3: Progreso de Cola (cuando posición <= 3) ==

note over QueueProc: Ejecuta cada 5 segundos
QueueProc -> DB: Recalcula posiciones de todos los tickets EN_ESPERA
QueueProc -> DB: UPDATE ticket SET positionInQueue = (nueva posición)
QueueProc -> DB: SELECT tickets WHERE positionInQueue <= 3 AND status = EN_ESPERA
DB --> QueueProc: [Ticket P01, posición: 3]
QueueProc -> DB: UPDATE ticket SET status = PROXIMO

note over Scheduler: Detecta Mensaje 2 programado
Scheduler -> Telegram: POST sendMessage ("⏰ Pronto será tu turno P01")
Telegram --> Scheduler: {ok: true}
Scheduler --> Cliente: Notificación Pre-aviso

== Fase 4: Asignación Automática a Asesor ==

QueueProc -> DB: SELECT advisors WHERE status=AVAILABLE ORDER BY assignedTicketsCount LIMIT 1
DB --> QueueProc: [Advisor: María González, moduleNumber: 3]
QueueProc -> DB: UPDATE ticket SET assignedAdvisor=María, assignedModuleNumber=3, status=ATENDIENDO
QueueProc -> DB: UPDATE advisor SET status=BUSY, assignedTicketsCount=assignedTicketsCount+1

note over Scheduler: Detecta Mensaje 3 programado
Scheduler -> Telegram: POST sendMessage ("🔔 ES TU TURNO P01! Módulo 3, Asesora: María González")
Telegram --> Scheduler: {ok: true}
Scheduler --> Cliente: Notificación Turno Activo

QueueProc -> Asesor: Notifica en terminal del asesor
Asesor --> Cliente: Atiende al cliente en módulo 3

== Fase 5: Completar Atención ==

Asesor -> Controller: PUT /api/admin/advisors/1/complete-ticket
Controller -> Service: completarTicket(ticketId)
Service -> DB: UPDATE ticket SET status=COMPLETADO
Service -> DB: UPDATE advisor SET status=AVAILABLE, assignedTicketsCount=assignedTicketsCount-1
Service -> DB: INSERT INTO auditoria (evento: TICKET_COMPLETADO)
Service --> Controller: {success: true}

@enduml
```

**Archivo fuente:** [02-sequence-diagram.puml](../diagrams/02-sequence-diagram.puml)

---

## Descripción de las Fases

### Fase 1: Creación de Ticket
- Cliente crea ticket en terminal
- Sistema calcula posición real en cola
- Programa 3 mensajes automáticos
- Retorna confirmación con tiempo estimado

### Fase 2: Envío de Mensaje 1 (Confirmación)
- Scheduler ejecuta cada 60 segundos
- Envía confirmación inmediata vía Telegram
- Incluye número de ticket, posición y tiempo estimado

### Fase 3: Progreso de Cola (Pre-aviso)
- QueueProcessor monitorea cada 5 segundos
- Recalcula posiciones en tiempo real
- Envía pre-aviso cuando posición ≤ 3

### Fase 4: Asignación Automática
- Selecciona asesor disponible con menor carga
- Actualiza estados (ticket → ATENDIENDO, asesor → BUSY)
- Envía notificación final con módulo y nombre del asesor

### Fase 5: Completar Atención
- Asesor marca ticket como completado
- Sistema libera recursos
- Registra auditoría del evento

---

## Participantes del Diagrama

| Participante | Tipo | Responsabilidad |
|--------------|------|-----------------|
| Cliente | Actor | Inicia el proceso, recibe notificaciones |
| Terminal | Sistema Externo | Interfaz de creación de tickets |
| TicketController | Componente | Maneja requests HTTP |
| TicketService | Componente | Lógica de negocio |
| TelegramService | Componente | Integración con Telegram |
| PostgreSQL | Base de Datos | Persistencia de datos |
| MessageScheduler | Scheduler | Envío asíncrono de mensajes |
| QueueProcessor | Scheduler | Procesamiento de colas |
| Advisor | Actor | Atiende al cliente |

---

## Interacciones Clave

### Creación Síncrona
- Terminal → Controller → Service → DB
- Respuesta inmediata al cliente
- Programación de mensajes asíncronos

### Procesamiento Asíncrono
- **MessageScheduler:** Cada 60 segundos
- **QueueProcessor:** Cada 5 segundos
- Independientes del flujo principal

### Notificaciones Push
- 3 mensajes automáticos vía Telegram
- Estados: PENDIENTE → ENVIADO/FALLIDO
- Reintentos automáticos en caso de fallo

---

## Validaciones

- ✅ Diagrama PlantUML válido y renderizable
- ✅ 9 participantes documentados
- ✅ 5 fases claramente separadas
- ✅ Notas explicativas en schedulers
- ✅ Flujo completo end-to-end
- ✅ ~30 interacciones documentadas

---

**Siguiente paso:** PASO 4 - Modelo de Datos ER