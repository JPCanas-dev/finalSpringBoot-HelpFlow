package com.helpflow.support_ticket_service.application.port.in;

import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;

import java.util.List;

// Consultas de tickets e historiales con validacion de permisos
public interface GetTicketPort {

    Ticket getTicketById(Long ticketId, Long currentUserId);

    List<Ticket> getTicketsForUser(Long currentUserId);

    List<TicketHistory> getHistoriesForTicket(Long ticketId, Long currentUserId);
}
