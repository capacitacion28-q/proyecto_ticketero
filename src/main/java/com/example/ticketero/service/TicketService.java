package com.example.ticketero.service;

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
        log.info("Creating ticket for nationalId: {}, queueType: {}", 
                request.nationalId(), request.queueType());

        // Generar número de ticket
        String numero = generateTicketNumber(request.queueType());
        
        // Calcular posición en cola
        int positionInQueue = calculateQueuePosition(request.queueType());
        
        // Calcular tiempo estimado
        int estimatedWaitMinutes = calculateEstimatedWait(request.queueType(), positionInQueue);

        // Crear ticket
        Ticket ticket = Ticket.builder()
                .codigoReferencia(UUID.randomUUID())
                .numero(numero)
                .nationalId(request.nationalId())
                .telefono(request.telefono())
                .branchOffice(request.branchOffice())
                .queueType(request.queueType())
                .status(TicketStatus.EN_ESPERA)
                .positionInQueue(positionInQueue)
                .estimatedWaitMinutes(estimatedWaitMinutes)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        
        // Programar mensaje de confirmación
        mensajeService.scheduleMessage(saved, MessageTemplate.TOTEM_TICKET_CREADO);
        
        log.info("Ticket created: {} at position {}", saved.getNumero(), positionInQueue);
        
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
        log.info("Updating ticket {} status to {}", ticketId, newStatus);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        
        // Programar mensajes según cambio de estado
        scheduleStatusChangeMessages(ticket, oldStatus, newStatus);
        
        log.info("Ticket {} status updated from {} to {}", ticketId, oldStatus, newStatus);
    }

    /**
     * Obtiene la posición actual en cola de un ticket
     */
    public QueuePositionResponse getQueuePosition(UUID codigoReferencia) {
        Ticket ticket = ticketRepository.findByCodigoReferencia(codigoReferencia)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + codigoReferencia));
        
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
        log.info("Assigning ticket {} to advisor {}", ticketId, advisorId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        
        // TODO: Asignar advisor cuando esté implementado
        ticket.setStatus(TicketStatus.ATENDIENDO);
        
        // Programar mensaje "es tu turno"
        mensajeService.scheduleMessage(ticket, MessageTemplate.TOTEM_ES_TU_TURNO);
        
        log.info("Ticket {} assigned to advisor {}", ticketId, advisorId);
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