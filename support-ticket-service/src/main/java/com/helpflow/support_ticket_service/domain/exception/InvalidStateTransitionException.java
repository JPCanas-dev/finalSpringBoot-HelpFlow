package com.helpflow.support_ticket_service.domain.exception;

// Cuando se intenta un cambio de estado no permitido
public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
