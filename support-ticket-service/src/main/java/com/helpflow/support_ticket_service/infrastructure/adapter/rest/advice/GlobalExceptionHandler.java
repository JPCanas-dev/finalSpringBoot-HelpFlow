package com.helpflow.support_ticket_service.infrastructure.adapter.rest.advice;

import com.helpflow.support_ticket_service.domain.exception.DomainException;
import com.helpflow.support_ticket_service.domain.exception.EntityNotFoundException;
import com.helpflow.support_ticket_service.domain.exception.InvalidStateTransitionException;
import com.helpflow.support_ticket_service.domain.exception.UnauthorizedTicketAccessException;
import com.helpflow.support_ticket_service.domain.exception.UserInactiveException;
import com.helpflow.support_ticket_service.infrastructure.adapter.metrics.TicketMetricsAdapter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Manejador global de excepciones con formato ProblemDetail RFC 7807 y métricas
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final TicketMetricsAdapter metricsAdapter;

    public GlobalExceptionHandler(TicketMetricsAdapter metricsAdapter) {
        this.metricsAdapter = metricsAdapter;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso no encontrado");
        problem.setType(URI.create("https://helpflow.com/errors/not-found"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(UserInactiveException.class)
    public ProblemDetail handleUserInactive(UserInactiveException ex, HttpServletRequest request) {
        metricsAdapter.incrementAuthFailure();
        log.warn("Usuario inactivo: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Usuario inactivo");
        problem.setType(URI.create("https://helpflow.com/errors/user-inactive"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ProblemDetail handleInvalidStateTransition(InvalidStateTransitionException ex, HttpServletRequest request) {
        log.warn("Transición de estado inválida: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Transición de estado no permitida");
        problem.setType(URI.create("https://helpflow.com/errors/invalid-state-transition"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(UnauthorizedTicketAccessException.class)
    public ProblemDetail handleUnauthorizedTicketAccess(UnauthorizedTicketAccessException ex, HttpServletRequest request) {
        metricsAdapter.incrementAccessDenied();
        log.warn("Acceso denegado a ticket: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Acceso no autorizado al ticket");
        problem.setType(URI.create("https://helpflow.com/errors/unauthorized-access"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex, HttpServletRequest request) {
        log.warn("Regla de negocio no cumplida: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Error de regla de negocio");
        problem.setType(URI.create("https://helpflow.com/errors/business-rule"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Error de validación en datos de entrada: {}", fieldErrors);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Existen campos inválidos en la petición");
        problem.setTitle("Error de validación");
        problem.setType(URI.create("https://helpflow.com/errors/validation-failed"));
        problem.setProperty("invalidFields", fieldErrors);
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        metricsAdapter.incrementAccessDenied();
        log.warn("Permisos insuficientes: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "No posee los permisos necesarios para realizar esta acción");
        problem.setTitle("Acceso denegado");
        problem.setType(URI.create("https://helpflow.com/errors/access-denied"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        metricsAdapter.incrementAuthFailure();
        log.warn("Credenciales inválidas: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciales de acceso incorrectas");
        problem.setTitle("Autenticación fallida");
        problem.setType(URI.create("https://helpflow.com/errors/bad-credentials"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en el servidor", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno en el servidor");
        problem.setTitle("Error interno");
        problem.setType(URI.create("https://helpflow.com/errors/internal-error"));
        enrichProblemDetail(problem, request);
        return problem;
    }

    // Agrega timestamp, instance y traceId requeridos por el RFC 7807
    private void enrichProblemDetail(ProblemDetail problem, HttpServletRequest request) {
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("traceId", UUID.randomUUID().toString());
    }
}
