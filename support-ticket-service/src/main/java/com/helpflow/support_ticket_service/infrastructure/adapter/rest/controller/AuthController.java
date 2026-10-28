package com.helpflow.support_ticket_service.infrastructure.adapter.rest.controller;

import com.helpflow.support_ticket_service.application.port.in.AuthenticateUserPort;
import com.helpflow.support_ticket_service.application.port.in.AuthenticateUserPort.AuthResponse;
import com.helpflow.support_ticket_service.application.port.in.AuthenticateUserPort.LoginCommand;
import com.helpflow.support_ticket_service.application.port.in.RegisterUserPort;
import com.helpflow.support_ticket_service.application.port.in.RegisterUserPort.RegisterUserCommand;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.AuthDto.LoginRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.AuthDto.RegisterRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.AuthDto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador para autenticación y registro de usuarios
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro y login con JWT")
public class AuthController {

    private final RegisterUserPort registerUserPort;
    private final AuthenticateUserPort authenticateUserPort;

    public AuthController(RegisterUserPort registerUserPort,
                          AuthenticateUserPort authenticateUserPort) {
        this.registerUserPort = registerUserPort;
        this.authenticateUserPort = authenticateUserPort;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario o técnico")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.document(),
                request.name(),
                request.email(),
                request.password(),
                request.role(),
                request.location(),
                request.department(),
                request.salary(),
                request.affiliationDate()
        );

        User created = registerUserPort.register(command);

        UserResponse response = new UserResponse(
                created.getId(),
                created.getDocument(),
                created.getName(),
                created.getEmail(),
                created.getRole().name(),
                created.getLocation(),
                created.getDepartment(),
                created.getStatus().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.email(), request.password());
        AuthResponse response = authenticateUserPort.authenticate(command);
        return ResponseEntity.ok(response);
    }
}
