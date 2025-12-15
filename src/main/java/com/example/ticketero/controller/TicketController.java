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
        log.info("Creating ticket for nationalId: {}, queueType: {}", 
                request.nationalId(), request.queueType());
        
        TicketResponse response = ticketService.create(request);
        
        log.info("Ticket created: {} at position {}", 
                response.numero(), response.positionInQueue());
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Consultar ticket por UUID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<TicketResponse> getByUuid(@PathVariable String uuid) {
        log.debug("Getting ticket by UUID: {}", uuid);
        
        try {
            UUID codigoReferencia = UUID.fromString(uuid);
            QueuePositionResponse position = ticketService.getQueuePosition(codigoReferencia);
            
            // Convertir a TicketResponse
            TicketResponse response = new TicketResponse(
                null, codigoReferencia, position.numero(), null, null,
                null, position.queueType(), position.status(),
                position.positionInQueue(), position.estimatedWaitMinutes(),
                position.assignedAdvisorName(), null, null
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Ticket not found for UUID: {}", uuid);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Consultar posición por número de ticket
     */
    @GetMapping("/{numero}/position")
    public ResponseEntity<QueuePositionResponse> getPositionByNumber(@PathVariable String numero) {
        log.debug("Getting position for ticket number: {}", numero);
        
        try {
            QueuePositionResponse response = ticketService.getQueuePositionByNumber(numero);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Ticket not found for number: {}", numero);
            return ResponseEntity.notFound().build();
        }
    }



    /**
     * Consultar tickets por cédula
     */
    @GetMapping("/by-national-id/{nationalId}")
    public ResponseEntity<List<TicketResponse>> getByNationalId(@PathVariable String nationalId) {
        log.debug("Getting tickets for nationalId: {}", nationalId);
        
        List<TicketResponse> tickets = ticketService.findByNationalId(nationalId);
        return ResponseEntity.ok(tickets);
    }
}