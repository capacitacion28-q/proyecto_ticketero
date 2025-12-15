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
     * Consultar ticket por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable Long id) {
        log.debug("Getting ticket by id: {}", id);
        
        return ticketService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Consultar posición en cola por código de referencia
     */
    @GetMapping("/position/{codigo}")
    public ResponseEntity<QueuePositionResponse> getQueuePosition(@PathVariable UUID codigo) {
        log.debug("Getting queue position for codigo: {}", codigo);
        
        try {
            QueuePositionResponse response = ticketService.getQueuePosition(codigo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Ticket not found for codigo: {}", codigo);
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