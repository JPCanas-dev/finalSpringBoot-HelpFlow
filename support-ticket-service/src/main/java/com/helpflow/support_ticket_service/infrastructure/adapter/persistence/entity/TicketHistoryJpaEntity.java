package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity;

import com.helpflow.support_ticket_service.domain.model.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Entidad JPA para el historial de cambios de tickets
@Entity
@Table(name = "ticket_histories")
public class TicketHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketJpaEntity ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_id", nullable = false)
    private UserJpaEntity modifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private TicketStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private TicketStatus newStatus;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TicketHistoryJpaEntity() {
    }

    public TicketHistoryJpaEntity(Long id, TicketJpaEntity ticket, UserJpaEntity modifiedBy,
                                  TicketStatus previousStatus, TicketStatus newStatus,
                                  String observations, LocalDateTime createdAt) {
        this.id = id;
        this.ticket = ticket;
        this.modifiedBy = modifiedBy;
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

    public TicketJpaEntity getTicket() {
        return ticket;
    }

    public void setTicket(TicketJpaEntity ticket) {
        this.ticket = ticket;
    }

    public UserJpaEntity getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UserJpaEntity modifiedBy) {
        this.modifiedBy = modifiedBy;
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
