package com.helpflow.support_ticket_service.application.port.in;

import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;

// Caso de uso para registrar nuevos usuarios o tecnicos
public interface RegisterUserPort {

    record RegisterUserCommand(
            String document,
            String name,
            String email,
            String password,
            Role role,
            String location,
            String department,
            BigDecimal salary,
            LocalDate affiliationDate
    ) {}

    User register(RegisterUserCommand command);
}
