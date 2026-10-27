package com.helpflow.support_ticket_service.domain.model;

// Estados posibles del ciclo de vida de un ticket
public enum TicketStatus {
    ABIERTO,
    ASIGNADO,
    EN_PROGRESO,
    RESUELTO,
    CERRADO,
    CANCELADO
}
