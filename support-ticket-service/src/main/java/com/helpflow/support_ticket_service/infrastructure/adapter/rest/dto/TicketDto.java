package com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto;

import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// DTOs para peticiones y respuestas de tickets
public class TicketDto {

    public record CreateTicketRequest(
            @NotBlank(message = "El titulo es obligatorio")
            @Size(max = 150, message = "El titulo no puede superar 150 caracteres")
            String title,

            @NotBlank(message = "La descripcion es obligatoria")
            String description,

            @NotNull(message = "La categoria es obligatoria")
            TicketCategory category,

            @NotNull(message = "La prioridad es obligatoria")
            TicketPriority priority,

            @NotBlank(message = "La sede es obligatoria")
            String location,

            String affectedAsset
    ) {}

    public record UpdateStatusRequest(
            @NotNull(message = "El nuevo estado es obligatorio")
            TicketStatus targetStatus,

            @NotBlank(message = "La observacion del cambio de estado es obligatoria")
            String observations
    ) {}

    public record ReassignTicketRequest(
            @NotNull(message = "El ID del nuevo tecnico es obligatorio")
            Long newAssigneeId,

            String observations
    ) {}

    public record TicketResponse(
            Long id,
            Long requesterId,
            Long assigneeId,
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority,
            String location,
            String affectedAsset,
            TicketStatus status,
            LocalDateTime createdAt,
            LocalDateTime slaDeadline,
            boolean slaBreached
    ) {}

    public record TicketHistoryResponse(
            Long id,
            Long ticketId,
            Long modifiedById,
            TicketStatus previousStatus,
            TicketStatus newStatus,
            String observations,
            LocalDateTime createdAt
    ) {}
}
