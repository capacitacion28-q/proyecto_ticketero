package com.example.ticketero.model.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO para dashboard administrativo
 */
public record DashboardResponse(
    SummaryStats summary,
    List<QueueStats> queueStats,
    List<AdvisorStats> advisorStats,
    LocalDateTime lastUpdated
) {
    
    public record SummaryStats(
        Integer totalTicketsToday,
        Integer ticketsInQueue,
        Integer ticketsBeingServed,
        Integer ticketsCompleted,
        Integer availableAdvisors,
        Double avgWaitTime
    ) {}
    
    public record QueueStats(
        String queueType,
        Integer ticketsWaiting,
        Integer avgWaitMinutes,
        Integer longestWaitMinutes
    ) {}
    
    public record AdvisorStats(
        Long advisorId,
        String name,
        String status,
        Integer moduleNumber,
        Integer ticketsServedToday,
        String currentTicketNumber
    ) {}
}