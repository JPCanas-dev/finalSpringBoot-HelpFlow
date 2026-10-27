package com.helpflow.support_ticket_service.domain.exception;

// Errores de reglas de negocio generales (correo repetido, documento duplicado, etc)
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
