package com.helpflow.support_ticket_service.application.port.in;

import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;

// Caso de uso para cambiar estado o reasignar ticket
public interface ManageTicketPort {

    record UpdateTicketStatusCommand(
            Long ticketId,
            Long currentUserId,
            TicketStatus targetStatus,
            String observations
    ) {}

    record ReassignTicketCommand(
            Long ticketId,
            Long adminUserId,
            Long newAssigneeId,
            String observations
    ) {}

    Ticket updateStatus(UpdateTicketStatusCommand command);

    Ticket reassignTicket(ReassignTicketCommand command);
}
