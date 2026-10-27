package com.helpflow.support_ticket_service.domain.exception;

// Cuando un usuario intenta acceder a un ticket ajeno sin permisos
public class UnauthorizedTicketAccessException extends DomainException {

    public UnauthorizedTicketAccessException(String message) {
        super(message);
    }
}
