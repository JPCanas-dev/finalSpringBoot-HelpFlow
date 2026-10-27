package com.helpflow.support_ticket_service.application.port.out;

import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Puerto de salida para acceso a datos de tickets
public interface TicketRepositoryPort {

    Ticket save(Ticket ticket);

    Optional<Ticket> findById(Long id);

    List<Ticket> findAll();

    List<Ticket> findByRequesterId(Long requesterId);

    List<Ticket> findByAssigneeId(Long assigneeId);

    long countActiveTicketsByAssigneeId(Long assigneeId);

    List<Ticket> findActiveTicketsNearDeadline(LocalDateTime threshold);

    long countBreachedSlaTickets();

    long countByCategoryAndPriority(String category, String priority);
}
