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
        log.trace("Starting queue processing job");
        
        try {
            queueProcessorService.processQueue();
            log.trace("Queue processing job completed");
        } catch (Exception e) {
            log.error("Error in queue processing job: {}", e.getMessage(), e);
        }
    }

    /**
     * Actualiza posiciones en cola cada 10 segundos
     */
    @Scheduled(fixedRate = 10000) // 10 segundos
    public void updateQueuePositions() {
        log.trace("Starting queue position update job");
        
        try {
            queueProcessorService.updateQueuePositions();
            log.trace("Queue position update job completed");
        } catch (Exception e) {
            log.error("Error in queue position update job: {}", e.getMessage(), e);
        }
    }
}