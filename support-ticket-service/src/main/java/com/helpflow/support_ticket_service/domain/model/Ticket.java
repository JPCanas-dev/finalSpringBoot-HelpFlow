package com.helpflow.support_ticket_service.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Entidad principal del dominio para tickets
public class Ticket {

    private Long id;
    private Long requesterId;
    private Long assigneeId;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private String location;
    private String affectedAsset;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime slaDeadline;

    public Ticket() {
    }

    public Ticket(Long id, Long requesterId, Long assigneeId, String title, String description,
                  TicketCategory category, TicketPriority priority, String location,
                  String affectedAsset, TicketStatus status, LocalDateTime createdAt,
                  LocalDateTime slaDeadline) {
        this.id = id;
        this.requesterId = requesterId;
        this.assigneeId = assigneeId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.location = location;
        this.affectedAsset = affectedAsset;
        this.status = status;
        this.createdAt = createdAt;
        this.slaDeadline = slaDeadline;
    }

    // Calcula la fecha limite sumando las horas de la prioridad
    public static LocalDateTime calculateSlaDeadline(LocalDateTime start, TicketPriority priority) {
        if (start == null || priority == null) {
            return LocalDateTime.now().plusHours(24);
        }
        return start.plusHours(priority.getSlaHours());
    }

    // Revisa si una transicion de estado es valida en el flujo del sistema
    public boolean canTransitionTo(TicketStatus targetStatus) {
        if (this.status == null || targetStatus == null) {
            return false;
        }
        if (this.status == targetStatus) {
            return true;
        }

        return switch (this.status) {
            case ABIERTO -> targetStatus == TicketStatus.ASIGNADO || targetStatus == TicketStatus.CANCELADO;
            case ASIGNADO -> targetStatus == TicketStatus.EN_PROGRESO || targetStatus == TicketStatus.CANCELADO;
            case EN_PROGRESO -> targetStatus == TicketStatus.RESUELTO || targetStatus == TicketStatus.CANCELADO;
            case RESUELTO -> targetStatus == TicketStatus.CERRADO || targetStatus == TicketStatus.EN_PROGRESO;
            case CERRADO, CANCELADO -> false; // Estados finales
        };
    }

    // Revisa si ya vencio el tiempo de SLA
    public boolean isSlaBreached() {
        if (this.slaDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.slaDeadline) && !isFinished();
    }

    // Revisa si esta proximo a vencer (por ejemplo en menos de 2 horas)
    public boolean isNearSlaBreach(int warningHours) {
        if (this.slaDeadline == null || isFinished()) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().plusHours(warningHours);
        return this.slaDeadline.isBefore(threshold);
    }

    public boolean isFinished() {
        return this.status == TicketStatus.RESUELTO ||
               this.status == TicketStatus.CERRADO ||
               this.status == TicketStatus.CANCELADO;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAffectedAsset() {
        return affectedAsset;
    }

    public void setAffectedAsset(String affectedAsset) {
        this.affectedAsset = affectedAsset;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }
}
