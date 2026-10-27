package com.helpflow.support_ticket_service.domain.model;

// Nivel de prioridad del ticket y horas maximas para el SLA
public enum TicketPriority {
    BAJA(48),
    MEDIA(24),
    ALTA(8),
    CRITICA(2);

    private final int slaHours;

    TicketPriority(int slaHours) {
        this.slaHours = slaHours;
    }

    public int getSlaHours() {
        return slaHours;
    }
}
