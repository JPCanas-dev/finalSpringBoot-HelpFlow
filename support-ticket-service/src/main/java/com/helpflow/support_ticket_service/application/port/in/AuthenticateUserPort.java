package com.helpflow.support_ticket_service.application.port.in;

// Caso de uso para login y generacion de token
public interface AuthenticateUserPort {

    record LoginCommand(
            String email,
            String password
    ) {}

    record AuthResponse(
            String token,
            String tokenType,
            Long userId,
            String email,
            String role
    ) {}

    AuthResponse authenticate(LoginCommand command);
}
