package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para crear un nuevo ticket
 */
public record TicketCreateRequest(
    
    @NotBlank(message = "El RUT/ID es obligatorio")
    String nationalId,
    
    @NotBlank(message = "El teléfono es obligatorio")
    String phoneNumber,
    
    @NotBlank(message = "La sucursal es obligatoria")
    String branchOffice,
    
    @NotNull(message = "El tipo de cola es obligatorio")
    QueueType queueType
) {}