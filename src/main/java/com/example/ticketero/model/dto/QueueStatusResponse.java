package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;

import java.util.List;

/**
 * Response DTO para estado de una cola específica
 */
public record QueueStatusResponse(
    QueueType queueType,
    String displayName,
    Integer totalTickets,
    Integer ticketsWaiting,
    Integer avgWaitMinutes,
    Integer availableAdvisors,
    List<TicketInQueue> ticketsInQueue
) {
    
    public record TicketInQueue(
        String numero,
        Integer positionInQueue,
        Integer estimatedWaitMinutes,
        String status
    ) {}
}