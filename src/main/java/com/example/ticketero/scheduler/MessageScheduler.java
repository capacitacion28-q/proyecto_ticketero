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
        log.debug("💬 [SCHEDULER] Processing pending messages...");
        
        try {
            int processed = mensajeService.processPendingMessages();
            if (processed > 0) {
                log.info("✅ [MESSAGES SENT] {} messages processed", processed);
            } else {
                log.debug("💬 [MESSAGES PROCESSED] No pending messages");
            }
        } catch (Exception e) {
            log.error("❌ [MESSAGE PROCESSING ERROR] {}", e.getMessage(), e);
        }
    }

    /**
     * Reintenta mensajes fallidos cada 5 minutos
     */
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void retryFailedMessages() {
        log.debug("🔁 [SCHEDULER] Retrying failed messages...");
        
        try {
            int retried = mensajeService.retryFailedMessages();
            if (retried > 0) {
                log.info("✅ [MESSAGES RETRIED] {} failed messages retried", retried);
            } else {
                log.debug("🔁 [RETRY COMPLETED] No failed messages to retry");
            }
        } catch (Exception e) {
            log.error("❌ [RETRY ERROR] {}", e.getMessage(), e);
        }
    }
}