package com.example.ticketero.service;

import com.example.ticketero.model.dto.DashboardResponse;
import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.AdvisorRepository;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para dashboard administrativo
 * Proporciona métricas y estadísticas del sistema
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final AdvisorRepository advisorRepository;

    /**
     * Obtiene métricas completas del dashboard
     */
    public DashboardResponse getDashboardMetrics() {
        log.debug("Generating dashboard metrics");
        
        // Métricas generales
        int totalTicketsToday = (int) ticketRepository.countTicketsToday();
        int activeTickets = (int) ticketRepository.countByStatus(TicketStatus.EN_ESPERA);
        int completedTicketsToday = (int) ticketRepository.countByStatus(TicketStatus.COMPLETADO);
        int availableAdvisors = 5; // Simplificado por ahora
        double avgWaitTime = 15.0; // Simplificado por ahora
        
        // Summary stats
        DashboardResponse.SummaryStats summary = new DashboardResponse.SummaryStats(
                totalTicketsToday,
                activeTickets,
                0, // ticketsBeingServed
                completedTicketsToday,
                availableAdvisors,
                avgWaitTime
        );
        
        // Queue stats
        List<DashboardResponse.QueueStats> queueStats = Arrays.stream(QueueType.values())
                .map(this::getQueueStats)
                .toList();
        
        // Advisor stats
        List<DashboardResponse.AdvisorStats> advisorStats = getAdvisorStats();
        
        return new DashboardResponse(
                summary,
                queueStats,
                advisorStats,
                LocalDateTime.now()
        );
    }

    /**
     * Obtiene el estado de una cola específica
     */
    public QueueStatusResponse getQueueStatus(QueueType queueType) {
        int waitingCount = (int) ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA);
        int proximoCount = (int) ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.PROXIMO);
        int atendiendoCount = (int) ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.ATENDIENDO);
        
        return new QueueStatusResponse(
                queueType,
                queueType.getDisplayName(),
                waitingCount + proximoCount + atendiendoCount,
                waitingCount,
                queueType.getAvgTimeMinutes(),
                1, // Simplificado
                List.of() // Vacío por ahora
        );
    }
    
    /**
     * Obtiene estadísticas de asesores
     */
    private List<DashboardResponse.AdvisorStats> getAdvisorStats() {
        return advisorRepository.findAll().stream()
                .map(advisor -> new DashboardResponse.AdvisorStats(
                        advisor.getId(),
                        advisor.getName(),
                        advisor.getStatus().name(),
                        advisor.getModuleNumber(),
                        advisor.getAssignedTicketsCount(),
                        null // currentTicket simplificado por ahora
                ))
                .toList();
    }
    
    /**
     * Obtiene estadísticas de una cola para el dashboard
     */
    private DashboardResponse.QueueStats getQueueStats(QueueType queueType) {
        int waitingCount = (int) ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA);
        int avgWaitMinutes = queueType.getAvgTimeMinutes();
        
        return new DashboardResponse.QueueStats(
                queueType.name(),
                waitingCount,
                avgWaitMinutes,
                avgWaitMinutes * 2 // Estimación del tiempo más largo
        );
    }


}