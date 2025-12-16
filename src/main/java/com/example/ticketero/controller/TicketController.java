package com.example.ticketero.controller;

import com.example.ticketero.model.dto.QueuePositionResponse;
import com.example.ticketero.model.dto.TicketCreateRequest;
import com.example.ticketero.model.dto.TicketResponse;
import com.example.ticketero.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller público para gestión de tickets
 * Endpoints accesibles desde tótems y aplicaciones cliente
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    /**
     * Crear nuevo ticket
     */
    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request) {
        log.info("🎫 [CREATE TICKET] Request: nationalId={}, queueType={}, phone={}", 
                request.nationalId(), request.queueType(), request.phone());
        
        try {
            TicketResponse response = ticketService.create(request);
            
            log.info("✅ [TICKET CREATED] Number: {}, Position: {}, UUID: {}, EstimatedWait: {}min", 
                    response.numero(), response.positionInQueue(), 
                    response.codigoReferencia(), response.estimatedWaitMinutes());
            
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            log.error("❌ [CREATE TICKET ERROR] nationalId={}, error: {}", 
                    request.nationalId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Consultar ticket por UUID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<TicketResponse> getByUuid(@PathVariable String uuid) {
        log.info("🔍 [GET TICKET] UUID: {}", uuid);
        
        try {
            UUID codigoReferencia = UUID.fromString(uuid);
            QueuePositionResponse position = ticketService.getQueuePosition(codigoReferencia);
            
            log.info("✅ [TICKET FOUND] Number: {}, Status: {}, Position: {}, Advisor: {}", 
                    position.numero(), position.status(), position.positionInQueue(), 
                    position.assignedAdvisorName());
            
            // Convertir a TicketResponse
            TicketResponse response = new TicketResponse(
                null, codigoReferencia, position.numero(), null, null,
                null, position.queueType(), position.status(),
                position.positionInQueue(), position.estimatedWaitMinutes(),
                position.assignedAdvisorName(), null, null
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("❌ [TICKET NOT FOUND] UUID: {}, error: {}", uuid, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Consultar posición por número de ticket
     */
    @GetMapping("/{numero}/position")
    public ResponseEntity<QueuePositionResponse> getPositionByNumber(@PathVariable String numero) {
        log.info("📍 [GET POSITION] Ticket number: {}", numero);
        
        try {
            QueuePositionResponse response = ticketService.getQueuePositionByNumber(numero);
            
            log.info("✅ [POSITION FOUND] Number: {}, Position: {}, Status: {}, Wait: {}min", 
                    response.numero(), response.positionInQueue(), 
                    response.status(), response.estimatedWaitMinutes());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("❌ [POSITION NOT FOUND] Number: {}, error: {}", numero, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }



    /**
     * Consultar tickets por cédula
     */
    @GetMapping("/by-national-id/{nationalId}")
    public ResponseEntity<List<TicketResponse>> getByNationalId(@PathVariable String nationalId) {
        log.info("👤 [GET BY NATIONAL ID] nationalId: {}", nationalId);
        
        try {
            List<TicketResponse> tickets = ticketService.findByNationalId(nationalId);
            
            log.info("✅ [TICKETS FOUND] nationalId: {}, count: {}", 
                    nationalId, tickets.size());
            
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("❌ [GET BY NATIONAL ID ERROR] nationalId: {}, error: {}", 
                    nationalId, e.getMessage());
            throw e;
        }
    }
}