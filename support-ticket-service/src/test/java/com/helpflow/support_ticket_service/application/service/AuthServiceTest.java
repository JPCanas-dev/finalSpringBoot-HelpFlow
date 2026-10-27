package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.AuthenticateUserPort.AuthResponse;
import com.helpflow.support_ticket_service.application.port.in.AuthenticateUserPort.LoginCommand;
import com.helpflow.support_ticket_service.application.port.in.RegisterUserPort.RegisterUserCommand;
import com.helpflow.support_ticket_service.application.port.out.PasswordEncoderPort;
import com.helpflow.support_ticket_service.application.port.out.TokenProviderPort;
import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.exception.BusinessRuleException;
import com.helpflow.support_ticket_service.domain.exception.UserInactiveException;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.domain.model.UserStatus;
import com.helpflow.support_ticket_service.infrastructure.adapter.metrics.TicketMetricsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias para el servicio de autenticacion y registro
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private TicketMetricsAdapter metricsAdapter;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setDocument("1017234890");
        sampleUser.setName("Juan Perez");
        sampleUser.setEmail("juan.perez@helpflow.com");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setRole(Role.ROLE_USUARIO);
        sampleUser.setStatus(UserStatus.ACTIVO);
    }

    @Test
    void register_ShouldSaveUser_WhenDataIsValid() {
        RegisterUserCommand command = new RegisterUserCommand(
                "1017234890",
                "Juan Perez",
                "juan.perez@helpflow.com",
                "12345",
                Role.ROLE_USUARIO,
                "SEDE_CENTRAL",
                "SISTEMAS",
                new BigDecimal("2500000"),
                LocalDate.now()
        );

        when(userRepositoryPort.existsByDocument("1017234890")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("juan.perez@helpflow.com")).thenReturn(false);
        when(passwordEncoderPort.encode("12345")).thenReturn("encodedPassword");
        when(userRepositoryPort.save(any(User.class))).thenReturn(sampleUser);

        User result = authService.register(command);

        assertNotNull(result);
        assertEquals("juan.perez@helpflow.com", result.getEmail());
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenDocumentAlreadyExists() {
        RegisterUserCommand command = new RegisterUserCommand(
                "1017234890",
                "Juan Perez",
                "juan.perez@helpflow.com",
                "12345",
                Role.ROLE_USUARIO,
                "SEDE_CENTRAL",
                "SISTEMAS",
                null,
                null
        );

        when(userRepositoryPort.existsByDocument("1017234890")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> authService.register(command));
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        LoginCommand command = new LoginCommand("juan.perez@helpflow.com", "12345");

        when(userRepositoryPort.findByEmail("juan.perez@helpflow.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoderPort.matches("12345", "encodedPassword")).thenReturn(true);
        when(tokenProviderPort.generateToken(sampleUser)).thenReturn("dummy-jwt-token");

        AuthResponse response = authService.authenticate(command);

        assertNotNull(response);
        assertEquals("dummy-jwt-token", response.token());
        assertEquals(1L, response.userId());
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserIsInactive() {
        sampleUser.setStatus(UserStatus.INACTIVO);
        LoginCommand command = new LoginCommand("juan.perez@helpflow.com", "12345");

        when(userRepositoryPort.findByEmail("juan.perez@helpflow.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(UserInactiveException.class, () -> authService.authenticate(command));
        verify(metricsAdapter).incrementAuthFailure();
    }
}
