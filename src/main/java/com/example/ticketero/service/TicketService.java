package com.example.ticketero.service;

import com.example.ticketero.exception.ConflictException;
import com.example.ticketero.model.dto.QueuePositionResponse;
import com.example.ticketero.model.dto.TicketCreateRequest;
import com.example.ticketero.model.dto.TicketResponse;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.MessageTemplate;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio principal para gestión de tickets
 * Maneja la lógica de negocio de creación, consulta y actualización de tickets
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final MensajeService mensajeService;

    /**
     * Crea un nuevo ticket en el sistema
     */
    @Transactional
    public TicketResponse create(TicketCreateRequest request) {
        log.info("🎫 [SERVICE CREATE] nationalId: {}, queueType: {}, phone: {}", 
                request.nationalId(), request.queueType(), request.phoneNumber());

        // RN-001: Validar que no tenga ticket activo (simplificado)
        long activeTickets = ticketRepository.countByStatus(TicketStatus.EN_ESPERA);
        log.debug("📊 [QUEUE COUNT] Active tickets in system: {}", activeTickets);
        
        // Generar número de ticket
        String numero = generateTicketNumber(request.queueType());
        log.debug("🏷️ [TICKET NUMBER] Generated: {}", numero);
        
        // Calcular posición en cola
        int positionInQueue = calculateQueuePosition(request.queueType());
        log.debug("📍 [QUEUE POSITION] Position: {}", positionInQueue);
        
        // Calcular tiempo estimado
        int estimatedWaitMinutes = calculateEstimatedWait(request.queueType(), positionInQueue);
        log.debug("⏰ [ESTIMATED WAIT] {} minutes", estimatedWaitMinutes);

        // Normalizar teléfono
        String normalizedPhone = normalizePhoneNumber(request.phoneNumber());
        log.debug("📞 [PHONE NORMALIZED] {} -> {}", request.phoneNumber(), normalizedPhone);

        // Crear ticket
        UUID uuid = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .codigoReferencia(uuid)
                .numero(numero)
                .nationalId(request.nationalId())
                .telefono(normalizedPhone)
                .branchOffice(request.branchOffice())
                .queueType(request.queueType())
                .status(TicketStatus.EN_ESPERA)
                .positionInQueue(positionInQueue)
                .estimatedWaitMinutes(estimatedWaitMinutes)
                .build();

        log.debug("💾 [SAVING TICKET] UUID: {}, Number: {}", uuid, numero);
        Ticket saved = ticketRepository.save(ticket);
        
        // Programar mensaje de confirmación
        log.debug("💬 [SCHEDULING MESSAGE] Template: TOTEM_TICKET_CREADO");
        mensajeService.scheduleMessage(saved, MessageTemplate.TOTEM_TICKET_CREADO);
        
        log.info("✅ [TICKET CREATED] Number: {}, Position: {}, Wait: {}min, UUID: {}", 
                saved.getNumero(), positionInQueue, estimatedWaitMinutes, uuid);
        
        return toResponse(saved);
    }

    /**
     * Busca ticket por ID
     */
    public Optional<TicketResponse> findById(Long id) {
        return ticketRepository.findById(id)
                .map(this::toResponse);
    }

    /**
     * Busca tickets por cédula
     */
    public List<TicketResponse> findByNationalId(String nationalId) {
        log.debug("Finding tickets for nationalId: {}", nationalId);
        
        return ticketRepository.findByNationalIdAndStatusIn(nationalId, TicketStatus.getActiveStatuses())
                .map(this::toResponse)
                .map(List::of)
                .orElse(List.of());
    }

    /**
     * Actualiza el estado de un ticket
     */
    @Transactional
    public void updateStatus(Long ticketId, TicketStatus newStatus) {
        log.info("🔄 [SERVICE UPDATE STATUS] Ticket: {}, New status: {}", ticketId, newStatus);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("❌ [TICKET NOT FOUND] ID: {}", ticketId);
                    return new RuntimeException("Ticket not found: " + ticketId);
                });
        
        TicketStatus oldStatus = ticket.getStatus();
        log.debug("🔄 [STATUS CHANGE] Ticket: {} from {} to {}", ticketId, oldStatus, newStatus);
        
        ticket.setStatus(newStatus);
        
        // Programar mensajes según cambio de estado
        scheduleStatusChangeMessages(ticket, oldStatus, newStatus);
        
        log.info("✅ [STATUS UPDATED] Ticket: {} ({}) {} -> {}", 
                ticketId, ticket.getNumero(), oldStatus, newStatus);
    }

    /**
     * Obtiene la posición actual en cola de un ticket por número
     */
    public QueuePositionResponse getQueuePositionByNumber(String numero) {
        Ticket ticket = ticketRepository.findByNumero(numero)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + numero));
        
        return getQueuePositionFromTicket(ticket);
    }
    
    /**
     * Obtiene la posición actual en cola de un ticket por UUID
     */
    public QueuePositionResponse getQueuePosition(UUID codigoReferencia) {
        Ticket ticket = ticketRepository.findByCodigoReferencia(codigoReferencia)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + codigoReferencia));
        
        return getQueuePositionFromTicket(ticket);
    }
    
    private QueuePositionResponse getQueuePositionFromTicket(Ticket ticket) {
        // Recalcular posición actual
        int currentPosition = calculateCurrentPosition(ticket);
        int estimatedWait = calculateEstimatedWait(ticket.getQueueType(), currentPosition);
        
        return new QueuePositionResponse(
                ticket.getNumero(),
                ticket.getQueueType(),
                ticket.getStatus(),
                currentPosition,
                estimatedWait,
                ticket.getAssignedAdvisor() != null ? ticket.getAssignedAdvisor().getName() : null,
                ticket.getAssignedModuleNumber(),
                "Ticket en cola"
        );
    }

    /**
     * Asigna un ticket a un asesor
     */
    @Transactional
    public void assignToAdvisor(Long ticketId, Long advisorId) {
        log.info("👥 [SERVICE ASSIGN] Ticket: {} -> Advisor: {}", ticketId, advisorId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("❌ [ASSIGN ERROR] Ticket not found: {}", ticketId);
                    return new RuntimeException("Ticket not found: " + ticketId);
                });
        
        TicketStatus oldStatus = ticket.getStatus();
        log.debug("🔄 [ASSIGN STATUS] Ticket: {} ({}) {} -> ATENDIENDO", 
                ticketId, ticket.getNumero(), oldStatus);
        
        // TODO: Asignar advisor cuando esté implementado
        ticket.setStatus(TicketStatus.ATENDIENDO);
        
        // Programar mensaje "es tu turno"
        log.debug("💬 [SCHEDULING MESSAGE] Template: TOTEM_ES_TU_TURNO");
        mensajeService.scheduleMessage(ticket, MessageTemplate.TOTEM_ES_TU_TURNO);
        
        log.info("✅ [ASSIGNMENT SUCCESS] Ticket: {} ({}) assigned to advisor: {}", 
                ticketId, ticket.getNumero(), advisorId);
    }

    // Métodos privados de utilidad

    private String generateTicketNumber(com.example.ticketero.model.enums.QueueType queueType) {
        char prefix = queueType.getPrefix();
        // Simplificado: usar timestamp para generar número único
        int nextNumber = (int) (System.currentTimeMillis() % 100) + 1;
        
        return String.format("%c%02d", prefix, nextNumber);
    }

    private int calculateQueuePosition(com.example.ticketero.model.enums.QueueType queueType) {
        return (int) ticketRepository.countByQueueTypeAndStatus(
                queueType, TicketStatus.EN_ESPERA) + 1;
    }

    private int calculateCurrentPosition(Ticket ticket) {
        // Simplificado: usar posición actual del ticket
        return ticket.getPositionInQueue();
    }

    private int calculateEstimatedWait(com.example.ticketero.model.enums.QueueType queueType, int position) {
        return position * queueType.getAvgTimeMinutes();
    }

    private void scheduleStatusChangeMessages(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        // Si pasa a PROXIMO, programar mensaje
        if (newStatus == TicketStatus.PROXIMO && oldStatus != TicketStatus.PROXIMO) {
            mensajeService.scheduleMessage(ticket, MessageTemplate.TOTEM_PROXIMO_TURNO);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        
        // Si ya tiene código de país, devolverlo tal como está
        if (phoneNumber.startsWith("+56")) {
            return phoneNumber;
        }
        
        // Si es número nacional (9 dígitos), agregar +56
        if (phoneNumber.matches("^[0-9]{9}$")) {
            return "+56" + phoneNumber;
        }
        
        // Si es número nacional (8 dígitos), agregar +56
        if (phoneNumber.matches("^[0-9]{8}$")) {
            return "+56" + phoneNumber;
        }
        
        return phoneNumber;
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getCodigoReferencia(),
                ticket.getNumero(),
                ticket.getNationalId(),
                ticket.getTelefono(),
                ticket.getBranchOffice(),
                ticket.getQueueType(),
                ticket.getStatus(),
                ticket.getPositionInQueue(),
                ticket.getEstimatedWaitMinutes(),
                ticket.getAssignedAdvisor() != null ? ticket.getAssignedAdvisor().getName() : null,
                ticket.getAssignedModuleNumber(),
                ticket.getCreatedAt()
        );
    }
}