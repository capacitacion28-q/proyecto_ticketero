package com.example.ticketero.scheduler;

import com.example.ticketero.service.MensajeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para procesamiento de mensajes Telegram
 * Ejecuta cada 60 segundos para enviar mensajes pendientes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {

    private final MensajeService mensajeService;

    /**
     * Procesa mensajes pendientes cada 60 segundos
     */
    @Scheduled(fixedRate = 60000) // 60 segundos
    public void processPendingMessages() {
        log.debug("Starting message processing job");
        
        try {
            mensajeService.processPendingMessages();
            log.debug("Message processing job completed");
        } catch (Exception e) {
            log.error("Error in message processing job: {}", e.getMessage(), e);
        }
    }

    /**
     * Reintenta mensajes fallidos cada 5 minutos
     */
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void retryFailedMessages() {
        log.debug("Starting failed message retry job");
        
        try {
            mensajeService.retryFailedMessages();
            log.debug("Failed message retry job completed");
        } catch (Exception e) {
            log.error("Error in failed message retry job: {}", e.getMessage(), e);
        }
    }
}