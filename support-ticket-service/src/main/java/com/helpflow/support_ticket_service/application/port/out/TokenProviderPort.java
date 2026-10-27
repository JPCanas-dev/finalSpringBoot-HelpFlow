package com.helpflow.support_ticket_service.application.port.out;

import com.helpflow.support_ticket_service.domain.model.User;

// Puerto de salida para generar y validar tokens JWT
public interface TokenProviderPort {

    String generateToken(User user);

    String extractEmail(String token);

    boolean validateToken(String token);
}
