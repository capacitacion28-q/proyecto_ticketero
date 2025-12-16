package com.example.ticketero.controller;

import com.example.ticketero.model.dto.DashboardResponse;
import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.service.AdvisorService;
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
    private final AdvisorService advisorService;

    /**
     * Obtener métricas del dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("📊 [DASHBOARD] Getting metrics");
        
        try {
            DashboardResponse response = dashboardService.getDashboardMetrics();
            
            log.info("✅ [DASHBOARD] Total tickets: {}, Active advisors: {}, Avg wait: {}min", 
                    response.summary().totalTickets(), 
                    response.summary().activeAdvisors(),
                    response.summary().averageWaitTime());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [DASHBOARD ERROR] {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtener estado de una cola específica
     */
    @GetMapping("/queue/{queueType}")
    public ResponseEntity<QueueStatusResponse> getQueueStatus(@PathVariable QueueType queueType) {
        log.info("📋 [QUEUE STATUS] Getting status for: {}", queueType);
        
        try {
            QueueStatusResponse response = dashboardService.getQueueStatus(queueType);
            
            log.info("✅ [QUEUE STATUS] {}: {} waiting, {} in progress, avg wait: {}min", 
                    queueType, response.waitingCount(), response.inProgressCount(), 
                    response.averageWaitTime());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [QUEUE STATUS ERROR] queueType: {}, error: {}", queueType, e.getMessage());
            throw e;
        }
    }

    /**
     * Actualizar estado de un ticket (administrativo)
     */
    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<Void> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status) {
        
        log.info("🔄 [ADMIN UPDATE STATUS] Ticket: {}, New status: {}", id, status);
        
        try {
            ticketService.updateStatus(id, status);
            
            log.info("✅ [STATUS UPDATED] Ticket: {} -> {}", id, status);
            
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("❌ [UPDATE STATUS FAILED] Ticket: {}, error: {}", id, e.getMessage());
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
        
        log.info("👥 [ADMIN ASSIGN] Ticket: {} -> Advisor: {}", ticketId, advisorId);
        
        try {
            ticketService.assignToAdvisor(ticketId, advisorId);
            
            log.info("✅ [ASSIGNMENT SUCCESS] Ticket: {} assigned to advisor: {}", ticketId, advisorId);
            
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("❌ [ASSIGNMENT FAILED] Ticket: {}, Advisor: {}, error: {}", 
                    ticketId, advisorId, e.getMessage());
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
    
    /**
     * Cambiar estado de un asesor
     */
    @PutMapping("/advisors/{id}/status")
    public ResponseEntity<Void> updateAdvisorStatus(
            @PathVariable Long id,
            @RequestBody UpdateAdvisorStatusRequest request) {
        
        log.info("👨‍💼 [ADMIN ADVISOR STATUS] Advisor: {}, New status: {}", id, request.status());
        
        try {
            advisorService.updateStatus(id, request.status());
            
            log.info("✅ [ADVISOR STATUS UPDATED] Advisor: {} -> {}", id, request.status());
            
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("❌ [ADVISOR STATUS FAILED] Advisor: {}, error: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    public record UpdateAdvisorStatusRequest(com.example.ticketero.model.enums.AdvisorStatus status) {}
}