package com.helpflow.support_ticket_service.domain.exception;

// Excepcion base para errores del dominio
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
