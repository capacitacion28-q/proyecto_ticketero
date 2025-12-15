package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO para ticket
 */
public record TicketResponse(
    Long id,
    UUID codigoReferencia,
    String numero,
    String nationalId,
    String telefono,
    String branchOffice,
    QueueType queueType,
    TicketStatus status,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    String assignedAdvisorName,
    Integer assignedModuleNumber,
    LocalDateTime createdAt
) {}