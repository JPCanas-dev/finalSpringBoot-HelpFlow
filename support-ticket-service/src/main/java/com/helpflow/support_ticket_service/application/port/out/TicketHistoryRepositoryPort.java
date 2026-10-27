package com.helpflow.support_ticket_service.application.port.out;

import com.helpflow.support_ticket_service.domain.model.TicketHistory;

import java.util.List;

// Puerto de salida para persistir historial de cambios
public interface TicketHistoryRepositoryPort {

    TicketHistory save(TicketHistory history);

    List<TicketHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
