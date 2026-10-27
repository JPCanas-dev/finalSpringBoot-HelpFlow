package com.helpflow.support_ticket_service.domain.model;

import java.time.LocalDateTime;

// Historial de auditoria de cambios de estado del ticket
public class TicketHistory {

    private Long id;
    private Long ticketId;
    private Long modifiedById;
    private TicketStatus previousStatus;
    private TicketStatus newStatus;
    private String observations;
    private LocalDateTime createdAt;

    public TicketHistory() {
    }

    public TicketHistory(Long id, Long ticketId, Long modifiedById, TicketStatus previousStatus,
                         TicketStatus newStatus, String observations, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.modifiedById = modifiedById;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.observations = observations;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getModifiedById() {
        return modifiedById;
    }

    public void setModifiedById(Long modifiedById) {
        this.modifiedById = modifiedById;
    }

    public TicketStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(TicketStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TicketStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
