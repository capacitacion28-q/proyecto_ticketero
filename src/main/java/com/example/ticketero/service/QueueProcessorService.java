package com.example.ticketero.service;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para procesamiento de colas
 * Maneja la asignación automática de tickets a asesores
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QueueProcessorService {

    private final TicketRepository ticketRepository;
    private final AdvisorService advisorService;
    private final TicketService ticketService;

    /**
     * Procesa la cola asignando tickets a asesores disponibles
     */
    @Transactional
    public int processQueue() {
        log.debug("Processing queue for ticket assignments");
        
        // Buscar asesor disponible
        Optional<Advisor> availableAdvisor = advisorService.findMostAvailable();
        
        if (availableAdvisor.isEmpty()) {
            log.debug("No available advisors found");
            return 0;
        }
        
        // Buscar próximo ticket en espera
        List<Ticket> waitingTickets = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA);
        
        if (waitingTickets.isEmpty()) {
            log.debug("No tickets waiting in queue");
            return 0;
        }
        
        // Asignar primer ticket al asesor
        Ticket ticket = waitingTickets.get(0);
        Advisor advisor = availableAdvisor.get();
        
        assignTicketToAdvisor(ticket, advisor);
        return 1;
    }

    /**
     * Actualiza posiciones en cola para todos los tickets activos
     */
    @Transactional
    public int updateQueuePositions() {
        log.debug("Updating queue positions");
        
        // Obtener tickets en espera ordenados por fecha de creación
        List<Ticket> waitingTickets = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA);
        
        int updatedCount = 0;
        for (int i = 0; i < waitingTickets.size(); i++) {
            Ticket ticket = waitingTickets.get(i);
            int newPosition = i + 1;
            
            if (ticket.getPositionInQueue() != newPosition) {
                ticket.setPositionInQueue(newPosition);
                
                // Calcular nuevo tiempo estimado
                int estimatedWait = newPosition * ticket.getQueueType().getAvgTimeMinutes();
                ticket.setEstimatedWaitMinutes(estimatedWait);
                
                // Si está en posición <= 3, cambiar a PROXIMO
                if (newPosition <= 3 && ticket.getStatus() == TicketStatus.EN_ESPERA) {
                    ticketService.updateStatus(ticket.getId(), TicketStatus.PROXIMO);
                }
                
                updatedCount++;
            }
        }
        
        log.debug("Updated positions for {} tickets", updatedCount);
        return updatedCount;
    }

    // Métodos privados

    private void assignTicketToAdvisor(Ticket ticket, Advisor advisor) {
        log.info("Assigning ticket {} to advisor {} (module {})", 
                ticket.getNumero(), advisor.getName(), advisor.getModuleNumber());
        
        // Asignar en el ticket
        ticketService.assignToAdvisor(ticket.getId(), advisor.getId());
        
        // Actualizar módulo asignado
        ticket.setAssignedModuleNumber(advisor.getModuleNumber());
        
        // Actualizar contador del asesor
        advisorService.assignTicket(advisor.getId(), ticket.getId());
        
        log.info("Ticket {} assigned to module {}", ticket.getNumero(), advisor.getModuleNumber());
    }
}