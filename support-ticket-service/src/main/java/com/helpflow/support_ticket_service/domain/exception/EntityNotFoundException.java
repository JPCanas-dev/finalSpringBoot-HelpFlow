package com.helpflow.support_ticket_service.domain.exception;

// Cuando no se encuentra un usuario o ticket en la base de datos
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
