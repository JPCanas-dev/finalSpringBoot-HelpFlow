package com.helpflow.support_ticket_service.domain.exception;

// Cuando un usuario esta inactivo e intenta realizar una accion
public class UserInactiveException extends DomainException {

    public UserInactiveException(String message) {
        super(message);
    }
}
