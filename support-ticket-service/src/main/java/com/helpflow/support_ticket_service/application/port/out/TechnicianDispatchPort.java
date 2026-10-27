package com.helpflow.support_ticket_service.application.port.out;

import com.helpflow.support_ticket_service.domain.model.TechnicianDispatchSuggestion;
import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;

import java.util.Optional;

// Puerto de salida para consultar el microservicio de disponibilidad tecnica
public interface TechnicianDispatchPort {

    Optional<TechnicianDispatchSuggestion> dispatchTechnician(Long ticketId, TicketCategory category, TicketPriority priority);
}
