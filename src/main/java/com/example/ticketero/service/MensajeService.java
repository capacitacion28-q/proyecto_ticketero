package com.example.ticketero.service;

import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.MessageTemplate;
import com.example.ticketero.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestión de mensajes Telegram
 * Maneja programación y envío de mensajes
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    /**
     * Programa un mensaje para envío
     */
    @Transactional
    public void scheduleMessage(Ticket ticket, MessageTemplate template) {
        log.info("Scheduling message {} for ticket {}", template, ticket.getNumero());
        
        LocalDateTime scheduledTime = calculateScheduledTime(template);
        
        Mensaje mensaje = Mensaje.builder()
                .ticket(ticket)
                .plantilla(template)
                .fechaProgramada(scheduledTime)
                .build();
        
        mensajeRepository.save(mensaje);
        
        log.debug("Message scheduled for {}", scheduledTime);
    }

    /**
     * Procesa mensajes pendientes de envío
     */
    @Transactional
    public int processPendingMessages() {
        // TODO: Implementar cuando estén los métodos del repository
        log.debug("Processing pending messages - TODO: implement");
        return 0; // Retornar 0 por ahora
    }

    /**
     * Marca un mensaje como enviado
     */
    @Transactional
    public void markAsSent(Long messageId) {
        Mensaje mensaje = mensajeRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));
        
        mensaje.setEstadoEnvio("ENVIADO");
        mensaje.setFechaEnvio(LocalDateTime.now());
        
        log.debug("Message {} marked as sent", messageId);
    }

    /**
     * Reintenta mensajes fallidos
     */
    @Transactional
    public int retryFailedMessages() {
        // TODO: Implementar cuando estén los métodos del repository
        log.debug("Retrying failed messages - TODO: implement");
        return 0; // Retornar 0 por ahora
    }

    // Métodos privados

    private LocalDateTime calculateScheduledTime(MessageTemplate template) {
        return switch (template) {
            case TOTEM_TICKET_CREADO -> LocalDateTime.now(); // Inmediato
            case TOTEM_PROXIMO_TURNO -> LocalDateTime.now().plusMinutes(2); // 2 min delay
            case TOTEM_ES_TU_TURNO -> LocalDateTime.now().plusMinutes(1); // 1 min delay
        };
    }

    private void sendMessage(Mensaje mensaje) {
        // TODO: Implementar envío real a Telegram
        log.info("Sending message {} to ticket {}", 
                mensaje.getPlantilla(), mensaje.getTicket().getNumero());
        
        // Simular envío exitoso por ahora
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Message sending interrupted", e);
        }
    }

    @Transactional
    public void incrementRetryCount(Long messageId) {
        Mensaje mensaje = mensajeRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));
        
        mensaje.setIntentos(mensaje.getIntentos() + 1);
        
        // Si supera 3 intentos, marcar como fallido
        if (mensaje.getIntentos() >= 3) {
            mensaje.setEstadoEnvio("FALLIDO");
            log.warn("Message {} marked as failed after {} attempts", messageId, mensaje.getIntentos());
        }
    }
}