package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity;

import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
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
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Entidad JPA para la tabla tickets con EntityGraph para evitar N+1
@Entity
@Table(name = "tickets")
@NamedEntityGraph(
        name = "Ticket.withUsers",
        attributeNodes = {
                @NamedAttributeNode("requester"),
                @NamedAttributeNode("assignee")
        }
)
public class TicketJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserJpaEntity requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserJpaEntity assignee;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TicketPriority priority;

    @Column(name = "location", nullable = false, length = 50)
    private String location;

    @Column(name = "affected_asset", length = 100)
    private String affectedAsset;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;

    public TicketJpaEntity() {
    }

    public TicketJpaEntity(Long id, UserJpaEntity requester, UserJpaEntity assignee,
                           String title, String description, TicketCategory category,
                           TicketPriority priority, String location, String affectedAsset,
                           TicketStatus status, LocalDateTime createdAt, LocalDateTime slaDeadline) {
        this.id = id;
        this.requester = requester;
        this.assignee = assignee;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserJpaEntity getRequester() {
        return requester;
    }

    public void setRequester(UserJpaEntity requester) {
        this.requester = requester;
    }

    public UserJpaEntity getAssignee() {
        return assignee;
    }

    public void setAssignee(UserJpaEntity assignee) {
        this.assignee = assignee;
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
