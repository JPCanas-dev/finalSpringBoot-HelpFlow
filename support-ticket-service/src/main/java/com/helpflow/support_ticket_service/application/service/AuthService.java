package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.AuthenticateUserPort;
import com.helpflow.support_ticket_service.application.port.in.RegisterUserPort;
import com.helpflow.support_ticket_service.application.port.out.PasswordEncoderPort;
import com.helpflow.support_ticket_service.application.port.out.TokenProviderPort;
import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.exception.BusinessRuleException;
import com.helpflow.support_ticket_service.domain.exception.EntityNotFoundException;
import com.helpflow.support_ticket_service.domain.exception.UserInactiveException;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.domain.model.UserStatus;
import com.helpflow.support_ticket_service.infrastructure.adapter.metrics.TicketMetricsAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Servicio de autenticación y registro de usuarios
@Service
public class AuthService implements RegisterUserPort, AuthenticateUserPort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProviderPort tokenProviderPort;
    private final TicketMetricsAdapter metricsAdapter;

    public AuthService(UserRepositoryPort userRepositoryPort,
                       PasswordEncoderPort passwordEncoderPort,
                       TokenProviderPort tokenProviderPort,
                       TicketMetricsAdapter metricsAdapter) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProviderPort = tokenProviderPort;
        this.metricsAdapter = metricsAdapter;
    }

    @Override
    @Transactional
    public User register(RegisterUserCommand command) {
        // Validar que documento no exista
        if (userRepositoryPort.existsByDocument(command.document())) {
            throw new BusinessRuleException("Ya existe un usuario registrado con el documento: " + command.document());
        }

        // Validar que correo corporativo no exista
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new BusinessRuleException("Ya existe un usuario registrado con el correo: " + command.email());
        }

        // Formato básico de correo corporativo helpflow.com
        if (!command.email().endsWith("@helpflow.com")) {
            throw new BusinessRuleException("El correo debe pertenecer al dominio corporativo @helpflow.com");
        }

        // Encriptar contraseña
        String encodedPassword = passwordEncoderPort.encode(command.password());

        // Asignar rol por defecto si no viene
        Role role = command.role() != null ? command.role() : Role.ROLE_USUARIO;

        User user = new User();
        user.setDocument(command.document());
        user.setName(command.name());
        user.setEmail(command.email().toLowerCase().trim());
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setLocation(command.location());
        user.setDepartment(command.department());
        user.setStatus(UserStatus.ACTIVO);
        user.setSalary(command.salary());
        user.setAffiliationDate(command.affiliationDate());
        user.setCreatedAt(LocalDateTime.now());

        return userRepositoryPort.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticate(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.email().toLowerCase().trim())
                .orElse(null);

        if (user == null) {
            metricsAdapter.incrementAuthFailure();
            throw new EntityNotFoundException("Credenciales inválidas para el correo: " + command.email());
        }

        // Verificar si el usuario está activo
        if (!user.isActive()) {
            metricsAdapter.incrementAuthFailure();
            throw new UserInactiveException("El usuario se encuentra inactivo en el sistema");
        }

        // Verificar contraseña
        if (!passwordEncoderPort.matches(command.password(), user.getPassword())) {
            metricsAdapter.incrementAuthFailure();
            throw new BusinessRuleException("Credenciales inválidas");
        }

        // Generar JWT
        String token = tokenProviderPort.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
