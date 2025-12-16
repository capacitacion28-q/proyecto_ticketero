package com.example.ticketero.scheduler;

import com.example.ticketero.service.QueueProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para procesamiento automático de colas
 * Ejecuta cada 5 segundos para asignar tickets y actualizar posiciones
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QueueProcessorScheduler {

    private final QueueProcessorService queueProcessorService;

    /**
     * Procesa la cola asignando tickets cada 5 segundos
     */
    @Scheduled(fixedRate = 5000) // 5 segundos
    public void processQueue() {
        log.debug("🔄 [SCHEDULER] Starting queue processing...");
        
        try {
            int processed = queueProcessorService.processQueue();
            if (processed > 0) {
                log.info("✅ [QUEUE PROCESSED] {} tickets processed", processed);
            } else {
                log.debug("🔄 [QUEUE PROCESSED] No tickets to process");
            }
        } catch (Exception e) {
            log.error("❌ [QUEUE PROCESSING ERROR] {}", e.getMessage(), e);
        }
    }

    /**
     * Actualiza posiciones en cola cada 10 segundos
     */
    @Scheduled(fixedRate = 10000) // 10 segundos
    public void updateQueuePositions() {
        log.debug("📍 [SCHEDULER] Updating queue positions...");
        
        try {
            int updated = queueProcessorService.updateQueuePositions();
            if (updated > 0) {
                log.info("✅ [POSITIONS UPDATED] {} tickets updated", updated);
            } else {
                log.debug("📍 [POSITIONS UPDATED] No positions to update");
            }
        } catch (Exception e) {
            log.error("❌ [POSITION UPDATE ERROR] {}", e.getMessage(), e);
        }
    }
}