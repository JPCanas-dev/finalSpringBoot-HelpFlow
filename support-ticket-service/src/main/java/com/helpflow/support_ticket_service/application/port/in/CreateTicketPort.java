package com.helpflow.support_ticket_service.application.port.in;

import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;

// Caso de uso para crear tickets
public interface CreateTicketPort {

    record CreateTicketCommand(
            Long requesterId,
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority,
            String location,
            String affectedAsset
    ) {}

    Ticket createTicket(CreateTicketCommand command);
}
