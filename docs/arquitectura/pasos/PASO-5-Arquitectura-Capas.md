# PASO 5: Arquitectura en Capas y Componentes Principales

**Proyecto:** Sistema Ticketero Digital  
**Fecha:** Diciembre 2025  
**Estado:** ✅ Completado

---

## Diagrama de Capas

```
┌─────────────────────────────────────────────────────────┐
│ CAPA DE PRESENTACIÓN (Controllers)                      │
│ - TicketController                                      │
│ - AdminController                                       │
│ - Recibe HTTP requests                                  │
│ - Valida con @Valid                                     │
│ - Retorna ResponseEntity<DTO>                           │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ CAPA DE NEGOCIO (Services)                              │
│ - TicketService                                         │
│ - TelegramService                                       │
│ - QueueManagementService                                │
│ - AdvisorService                                        │
│ - NotificationService                                   │
│ - Lógica de negocio                                     │
│ - Transacciones (@Transactional)                        │
│ - Orquestación de operaciones                           │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ CAPA DE DATOS (Repositories)                            │
│ - TicketRepository extends JpaRepository                │
│ - MensajeRepository                                     │
│ - AdvisorRepository                                     │
│ - Queries custom con @Query                             │
│ - Spring Data JPA                                       │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ BASE DE DATOS (PostgreSQL)                              │
│ - ticket (tabla principal)                              │
│ - mensaje (mensajes programados)                        │
│ - advisor (asesores)                                    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ CAPA ASÍNCRONA (Schedulers)                             │
│ - MessageScheduler (@Scheduled fixedRate=60s)           │
│ - QueueProcessorScheduler (@Scheduled fixedRate=5s)     │
│ - Procesamiento en background                           │
└─────────────────────────────────────────────────────────┘
```

---

## Responsabilidades por Capa

### 1. Controllers (Capa de Presentación)
**Responsabilidad:** Manejar HTTP requests/responses  
**Prohibido:** Lógica de negocio, acceso directo a DB

```java
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    
    @PostMapping
    public ResponseEntity<TicketResponse> crearTicket(@Valid @RequestBody TicketRequest request) {
        // 1. Validar (automático con @Valid)
        // 2. Delegar a service
        // 3. Retornar response
    }
}
```

### 2. Services (Capa de Negocio)
**Responsabilidad:** Lógica de negocio, transacciones, orquestación  
**Prohibido:** Lógica de presentación (HTTP codes), SQL directo

```java
@Service
@Transactional
public class TicketService {
    
    public TicketResponse crearTicket(TicketRequest request) {
        // 1. Validar reglas de negocio (RN-001: único ticket activo)
        // 2. Generar número de ticket
        // 3. Calcular posición (RN-010)
        // 4. Persistir ticket
        // 5. Programar 3 mensajes
        // 6. Registrar auditoría (RN-011)
        // 7. Retornar response
    }
}
```

### 3. Repositories (Capa de Datos)
**Responsabilidad:** Acceso a datos, queries  
**Prohibido:** Lógica de negocio

```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    @Query("SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt ASC")
    List<Ticket> findByStatusOrderByCreatedAtAsc(@Param("status") String status);
}
```

### 4. Schedulers (Capa Asíncrona)
**Responsabilidad:** Procesamiento en background  
**Prohibido:** HTTP requests directos de clientes

```java
@Component
public class MessageScheduler {
    
    @Scheduled(fixedRate = 60000) // Cada 60 segundos
    public void procesarMensajesPendientes() {
        // 1. Buscar mensajes con estado=PENDIENTE y fechaProgramada <= NOW
        // 2. Enviar vía TelegramService
        // 3. Actualizar estado a ENVIADO/FALLIDO
    }
}
```

---

## Componentes Principales

### TicketController
**Responsabilidad:** Exponer API REST para gestión de tickets

**Endpoints:**
- `POST /api/tickets` - Crear ticket (RF-001)
- `GET /api/tickets/{uuid}` - Obtener ticket (RF-006)
- `GET /api/tickets/{numero}/position` - Consultar posición (RF-003)

**Dependencias:**
- TicketService

**Validaciones:**
- @Valid en TicketRequest (Bean Validation)
- Códigos HTTP apropiados (201 Created, 409 Conflict, 400 Bad Request)

### AdminController
**Responsabilidad:** Panel administrativo para supervisores

**Endpoints:**
- `GET /api/admin/dashboard` - Dashboard completo (RF-007)
- `GET /api/admin/queues/{type}` - Estado de cola (RF-005)
- `GET /api/admin/advisors` - Lista de asesores (RF-007)
- `PUT /api/admin/advisors/{id}/status` - Cambiar estado asesor

**Dependencias:**
- QueueManagementService
- AdvisorService
- TicketRepository

**Actualización:** Dashboard se actualiza cada 5 segundos (RNF-002)

### TicketService
**Responsabilidad:** Lógica de negocio para tickets

**Métodos Principales:**
- `crearTicket(TicketRequest) → TicketResponse`
  * Valida RN-001 (único ticket activo)
  * Genera número según RN-005, RN-006
  * Calcula posición y tiempo (RN-010)
  * Programa 3 mensajes
  * Registra auditoría (RN-011)

- `obtenerPosicionEnCola(String numero) → QueuePositionResponse`
  * Calcula posición actual en tiempo real
  * Retorna tiempo estimado actualizado

**Dependencias:**
- TicketRepository
- MensajeRepository
- TelegramService (para programar mensajes)

**Transacciones:** @Transactional para operaciones de escritura

### TelegramService
**Responsabilidad:** Integración con Telegram Bot API

**Métodos Principales:**
- `enviarMensaje(String chatId, String texto) → String messageId`
  * POST a https://api.telegram.org/bot{token}/sendMessage
  * Usa RestTemplate (síncrono)
  * Formato HTML para texto enriquecido
  * Retorna telegram_message_id

- `obtenerTextoMensaje(String plantilla, String numeroTicket) → String`
  * Genera texto según plantilla (totem_ticket_creado, etc.)
  * Usa emojis (✅, ⏰, 🔔)

**Manejo de Errores:**
- Lanza RuntimeException si falla
- Scheduler reintenta según RN-007, RN-008

### QueueManagementService
**Responsabilidad:** Gestión de colas y asignación automática

**Métodos Principales:**
- `asignarSiguienteTicket() → void`
  * Ejecutado por QueueProcessorScheduler cada 5s
  * Selecciona asesor AVAILABLE con menor carga (RN-004)
  * Prioriza colas según RN-002 (GERENCIA > EMPRESAS > PERSONAL_BANKER > CAJA)
  * Dentro de cola: orden FIFO (RN-003)
  * Actualiza estado ticket a ATENDIENDO
  * Actualiza estado asesor a BUSY

- `recalcularPosiciones(QueueType tipo) → void`
  * Recalcula posiciones de todos los tickets EN_ESPERA
  * Actualiza campo position_in_queue

**Dependencias:**
- TicketRepository
- AdvisorRepository
- NotificationService (para Mensaje 3)

### AdvisorService
**Responsabilidad:** Gestión de asesores/ejecutivos

**Métodos Principales:**
- `obtenerAsesorDisponible() → Advisor`
- `cambiarEstadoAsesor(Long id, AdvisorStatus status) → void`
- `completarTicket(Long advisorId) → void`

### NotificationService
**Responsabilidad:** Orquestación de notificaciones

**Métodos Principales:**
- `programarMensajes(Ticket ticket) → void`
- `enviarMensajeInmediato(Ticket ticket, MessageTemplate template) → void`

### MessageScheduler
**Responsabilidad:** Envío asíncrono de mensajes programados

**Configuración:**
- `@Scheduled(fixedRate = 60000)` // Cada 60 segundos
- `@EnableScheduling` en clase principal

**Lógica:**
1. Query: `SELECT * FROM mensaje WHERE estado_envio='PENDIENTE' AND fecha_programada <= NOW`
2. Para cada mensaje:
   - TelegramService.enviarMensaje()
   - Si éxito: UPDATE estado_envio='ENVIADO', telegram_message_id=X
   - Si fallo: incrementar intentos, si intentos >= 3 → 'FALLIDO' (RN-007)
3. Reintentos con backoff: 30s, 60s, 120s (RN-008)

**Manejo de Errores:**
- Try-catch por mensaje (un fallo no detiene el scheduler)
- Logging detallado para debugging

### QueueProcessorScheduler
**Responsabilidad:** Procesamiento automático de colas

**Configuración:**
- `@Scheduled(fixedRate = 5000)` // Cada 5 segundos

**Lógica:**
1. Recalcular posiciones de todos los tickets EN_ESPERA
2. Identificar tickets con posición <= 3 → UPDATE status='PROXIMO' (RN-012)
3. Buscar asesores AVAILABLE
4. Si hay asesor disponible:
   - QueueManagementService.asignarSiguienteTicket()
5. Registrar auditoría de asignaciones

---

## Dependencias Entre Componentes

```
Controllers
    ↓
Services ← Schedulers
    ↓
Repositories
    ↓
Database
```

**Reglas de Dependencia:**
- Controllers solo dependen de Services
- Services pueden depender de otros Services y Repositories
- Schedulers dependen de Services (no Controllers)
- Repositories solo dependen de Database
- No dependencias circulares

---

## Validaciones

- ✅ Diagrama ASCII de capas incluido
- ✅ 5 capas documentadas (Presentación, Negocio, Datos, BD, Asíncrona)
- ✅ Responsabilidades por capa claras
- ✅ 9 componentes documentados
- ✅ Ejemplos de código para cada componente
- ✅ Dependencias entre componentes especificadas
- ✅ Reglas de arquitectura definidas

---

**Siguiente paso:** PASO 6 - Decisiones Arquitectónicas (ADRs)