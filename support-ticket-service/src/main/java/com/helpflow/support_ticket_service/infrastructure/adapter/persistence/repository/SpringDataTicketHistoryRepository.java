package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository;

import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.TicketHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio Spring Data JPA para historiales
@Repository
public interface SpringDataTicketHistoryRepository extends JpaRepository<TicketHistoryJpaEntity, Long> {

    @Query("SELECT h FROM TicketHistoryJpaEntity h JOIN FETCH h.modifiedBy WHERE h.ticket.id = :ticketId ORDER BY h.createdAt DESC")
    List<TicketHistoryJpaEntity> findByTicketIdOrderByCreatedAtDesc(@Param("ticketId") Long ticketId);
}
