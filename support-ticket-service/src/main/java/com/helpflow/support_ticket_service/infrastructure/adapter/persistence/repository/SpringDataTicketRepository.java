package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository;

import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.TicketJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Repositorio Spring Data JPA para tickets con EntityGraph y queries optimizadas
@Repository
public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, Long> {

    @Override
    @EntityGraph(value = "Ticket.withUsers", type = EntityGraph.EntityGraphType.LOAD)
    Optional<TicketJpaEntity> findById(Long id);

    @Override
    @EntityGraph(value = "Ticket.withUsers", type = EntityGraph.EntityGraphType.LOAD)
    List<TicketJpaEntity> findAll();

    @EntityGraph(value = "Ticket.withUsers", type = EntityGraph.EntityGraphType.LOAD)
    List<TicketJpaEntity> findByRequesterId(Long requesterId);

    @EntityGraph(value = "Ticket.withUsers", type = EntityGraph.EntityGraphType.LOAD)
    List<TicketJpaEntity> findByAssigneeId(Long assigneeId);

    @Query("SELECT COUNT(t) FROM TicketJpaEntity t WHERE t.assignee.id = :assigneeId AND t.status NOT IN ('RESUELTO', 'CERRADO', 'CANCELADO')")
    long countActiveTicketsByAssigneeId(@Param("assigneeId") Long assigneeId);

    @EntityGraph(value = "Ticket.withUsers", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT t FROM TicketJpaEntity t WHERE t.status NOT IN ('RESUELTO', 'CERRADO', 'CANCELADO') AND t.slaDeadline <= :threshold")
    List<TicketJpaEntity> findActiveTicketsNearDeadline(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(t) FROM TicketJpaEntity t WHERE t.status NOT IN ('RESUELTO', 'CERRADO', 'CANCELADO') AND t.slaDeadline < CURRENT_TIMESTAMP")
    long countBreachedSlaTickets();

    @Query("SELECT COUNT(t) FROM TicketJpaEntity t WHERE t.category = :category AND t.priority = :priority")
    long countByCategoryAndPriority(@Param("category") TicketCategory category, @Param("priority") TicketPriority priority);
}
