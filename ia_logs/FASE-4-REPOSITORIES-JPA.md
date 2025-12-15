# FASE 4 - REPOSITORIES JPA INTERFACES

## OBJETIVO COMPLETADO ✅
Crear interfaces JPA Repository con queries personalizadas usando Java 21 Text Blocks para el sistema ticketero.

## ARCHIVOS CREADOS

### 1. TicketRepository.java
```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
   
   // Query derivada simple
   List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);
   
   // Query personalizada con Text Block
   @Query("""
       SELECT t FROM Ticket t 
       WHERE t.nationalId = :nationalId 
       AND t.status IN :statuses
       ORDER BY t.createdAt DESC
       """)
   List<Ticket> findByNationalIdAndStatusIn(
       @Param("nationalId") String nationalId,
       @Param("statuses") List<TicketStatus> statuses
   );
   
   // Conteo por estado
   long countByStatus(TicketStatus status);
   
   // Posición en cola
   @Query("""
       SELECT COUNT(t) FROM Ticket t 
       WHERE t.queueType = :queueType 
       AND t.status = 'WAITING' 
       AND t.createdAt < :createdAt
       """)
   long countTicketsAheadInQueue(
       @Param("queueType") QueueType queueType,
       @Param("createdAt") LocalDateTime createdAt
   );
}
```

### 2. MensajeRepository.java
```java
@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
   
   // Mensajes pendientes para Telegram
   List<Mensaje> findByEnviadoFalseOrderByCreatedAtAsc();
   
   // Mensajes con reintentos
   @Query("""
       SELECT m FROM Mensaje m 
       WHERE m.enviado = false 
       AND m.intentos < :maxIntentos
       ORDER BY m.createdAt ASC
       """)
   List<Mensaje> findPendingMessagesWithRetries(@Param("maxIntentos") int maxIntentos);
   
   // Limpiar mensajes antiguos enviados
   @Modifying
   @Query("DELETE FROM Mensaje m WHERE m.enviado = true AND m.createdAt < :cutoffDate")
   void deleteOldSentMessages(@Param("cutoffDate") LocalDateTime cutoffDate);
}
```

### 3. AdvisorRepository.java
```java
@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
   
   // Advisors disponibles
   List<Advisor> findByStatusOrderByCurrentTicketsAsc(AdvisorStatus status);
   
   // Advisor más disponible
   @Query("""
       SELECT a FROM Advisor a 
       WHERE a.status = 'AVAILABLE' 
       ORDER BY a.currentTickets ASC, a.totalTicketsHandled ASC
       LIMIT 1
       """)
   Optional<Advisor> findMostAvailableAdvisor();
   
   // Estadísticas de advisors
   @Query("""
       SELECT COUNT(a) FROM Advisor a 
       WHERE a.status = :status
       """)
   long countByStatus(@Param("status") AdvisorStatus status);
}
```

## CARACTERÍSTICAS IMPLEMENTADAS

### ✅ Java 21 Text Blocks
- Queries multilinea legibles
- Sintaxis SQL clara y mantenible
- Parámetros nombrados con @Param

### ✅ Query Derivadas Spring Data
- `findByStatusOrderByCreatedAtAsc`
- `findByEnviadoFalseOrderByCreatedAtAsc`
- `countByStatus`

### ✅ Queries Personalizadas @Query
- Búsquedas complejas con múltiples condiciones
- Conteos y estadísticas
- Optimización de asignación de advisors

### ✅ Operaciones de Modificación
- `@Modifying` para DELETE operations
- Limpieza automática de mensajes antiguos

## PATRONES APLICADOS

### 1. Repository Pattern
- Interfaces que extienden JpaRepository
- Separación clara de responsabilidades
- Solo operaciones de acceso a datos

### 2. Query Optimization
- LIMIT 1 para búsquedas de un solo resultado
- ORDER BY para resultados consistentes
- Índices implícitos en campos de búsqueda

### 3. Parameter Binding
- @Param para parámetros nombrados
- Type-safe parameter binding
- Prevención de SQL injection

## COMPILACIÓN EXITOSA ✅
```bash
mvn clean compile
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 16 source files to target/classes
[INFO] BUILD SUCCESS
```

## PRÓXIMA FASE
**FASE 5 - Services (Lógica de Negocio)**
- TicketService con operaciones CRUD
- MensajeService para Telegram
- AdvisorService para asignación
- Transacciones y validaciones

## NOTAS TÉCNICAS
- Todas las queries usan parámetros nombrados
- Text Blocks mejoran legibilidad del SQL
- Repositories siguen convenciones Spring Data
- Preparados para inyección en Services

---
**Fecha:** $(date)
**Archivos:** 3 repositories creados
**Status:** ✅ COMPLETADO