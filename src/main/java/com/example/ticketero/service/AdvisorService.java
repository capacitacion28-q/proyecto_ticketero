package com.example.ticketero.service;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.repository.AdvisorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio para gestión de asesores
 * Maneja asignación de tickets y estados de asesores
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdvisorService {

    private final AdvisorRepository advisorRepository;

    /**
     * Encuentra el asesor más disponible (menos tickets asignados)
     */
    public Optional<Advisor> findMostAvailable() {
        return advisorRepository.findMostAvailableAdvisor();
    }

    /**
     * Asigna un ticket a un asesor
     */
    @Transactional
    public void assignTicket(Long advisorId, Long ticketId) {
        log.info("Assigning ticket {} to advisor {}", ticketId, advisorId);
        
        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new RuntimeException("Advisor not found: " + advisorId));
        
        // Incrementar contador de tickets asignados
        advisor.setAssignedTicketsCount(advisor.getAssignedTicketsCount() + 1);
        
        // Si es el primer ticket, cambiar estado a BUSY
        if (advisor.getAssignedTicketsCount() == 1 && advisor.getStatus() == AdvisorStatus.AVAILABLE) {
            advisor.setStatus(AdvisorStatus.BUSY);
        }
        
        log.info("Advisor {} now has {} assigned tickets", advisorId, advisor.getAssignedTicketsCount());
    }

    /**
     * Libera un ticket de un asesor
     */
    @Transactional
    public void releaseTicket(Long advisorId) {
        log.info("Releasing ticket from advisor {}", advisorId);
        
        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new RuntimeException("Advisor not found: " + advisorId));
        
        // Decrementar contador
        if (advisor.getAssignedTicketsCount() > 0) {
            advisor.setAssignedTicketsCount(advisor.getAssignedTicketsCount() - 1);
        }
        
        // Si no tiene más tickets, cambiar a AVAILABLE
        if (advisor.getAssignedTicketsCount() == 0 && advisor.getStatus() == AdvisorStatus.BUSY) {
            advisor.setStatus(AdvisorStatus.AVAILABLE);
        }
        
        log.info("Advisor {} now has {} assigned tickets", advisorId, advisor.getAssignedTicketsCount());
    }

    /**
     * Actualiza el estado de un asesor
     */
    @Transactional
    public void updateStatus(Long advisorId, AdvisorStatus newStatus) {
        log.info("Updating advisor {} status to {}", advisorId, newStatus);
        
        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new RuntimeException("Advisor not found: " + advisorId));
        
        advisor.setStatus(newStatus);
        
        log.info("Advisor {} status updated to {}", advisorId, newStatus);
    }
}