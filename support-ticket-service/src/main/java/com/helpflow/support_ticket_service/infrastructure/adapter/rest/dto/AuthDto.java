package com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto;

import com.helpflow.support_ticket_service.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTOs para operaciones de autenticacion
public class AuthDto {

    public record RegisterRequest(
            @NotBlank(message = "El documento es obligatorio")
            String document,

            @NotBlank(message = "El nombre es obligatorio")
            String name,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El formato de correo no es valido")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@helpflow\\.com$", message = "El correo debe pertenecer al dominio @helpflow.com")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            String password,

            Role role,

            @NotBlank(message = "La sede es obligatoria")
            String location,

            @NotBlank(message = "El departamento o area es obligatorio")
            String department,

            @PositiveOrZero(message = "El salario no puede ser negativo")
            BigDecimal salary,

            LocalDate affiliationDate
    ) {}

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El formato de correo no es valido")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            String password
    ) {}

    public record AuthResponse(
            String token,
            String tokenType,
            Long userId,
            String email,
            String role
    ) {}

    public record UserResponse(
            Long id,
            String document,
            String name,
            String email,
            String role,
            String location,
            String department,
            String status
    ) {}
}
