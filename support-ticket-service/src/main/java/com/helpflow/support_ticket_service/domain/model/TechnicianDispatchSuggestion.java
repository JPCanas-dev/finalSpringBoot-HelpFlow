package com.helpflow.support_ticket_service.domain.model;

// Respuesta que llega desde technician-dispatch-service
public record TechnicianDispatchSuggestion(
        Long ticketId,
        String tecnicoId,
        TechnicianAvailability disponibilidad,
        Integer tiempoEstimadoHoras
) {
}
