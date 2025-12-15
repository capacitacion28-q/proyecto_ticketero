package com.example.ticketero.controller;

import com.example.ticketero.model.dto.DashboardResponse;
import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.service.DashboardService;
import com.example.ticketero.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller administrativo para dashboard y gestión
 * Endpoints para personal administrativo y supervisores
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;

    /**
     * Obtener métricas del dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.debug("Getting dashboard metrics");
        
        DashboardResponse response = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener estado de una cola específica
     */
    @GetMapping("/queue/{queueType}")
    public ResponseEntity<QueueStatusResponse> getQueueStatus(@PathVariable QueueType queueType) {
        log.debug("Getting queue status for: {}", queueType);
        
        QueueStatusResponse response = dashboardService.getQueueStatus(queueType);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar estado de un ticket (administrativo)
     */
    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<Void> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status) {
        
        log.info("Admin updating ticket {} status to {}", id, status);
        
        try {
            ticketService.updateStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("Failed to update ticket {} status: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Asignar ticket a asesor (administrativo)
     */
    @PutMapping("/tickets/{ticketId}/assign/{advisorId}")
    public ResponseEntity<Void> assignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long advisorId) {
        
        log.info("Admin assigning ticket {} to advisor {}", ticketId, advisorId);
        
        try {
            ticketService.assignToAdvisor(ticketId, advisorId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("Failed to assign ticket {}: {}", ticketId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtener lista de asesores
     */
    @GetMapping("/advisors")
    public ResponseEntity<List<DashboardResponse.AdvisorStats>> getAdvisors() {
        log.debug("Getting all advisors");
        
        DashboardResponse dashboard = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(dashboard.advisorStats());
    }
    
    /**
     * Obtener estadísticas de asesores
     */
    @GetMapping("/advisors/stats")
    public ResponseEntity<List<DashboardResponse.AdvisorStats>> getAdvisorStats() {
        log.debug("Getting advisor statistics");
        
        DashboardResponse dashboard = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(dashboard.advisorStats());
    }
    
    /**
     * Obtener resumen simplificado del dashboard
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse.SummaryStats> getSummary() {
        log.debug("Getting dashboard summary");
        
        DashboardResponse dashboard = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(dashboard.summary());
    }
}