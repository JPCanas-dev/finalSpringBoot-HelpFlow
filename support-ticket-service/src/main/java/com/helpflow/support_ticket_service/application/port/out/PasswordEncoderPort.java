package com.helpflow.support_ticket_service.application.port.out;

// Puerto de salida para encriptar y verificar contrasenas
public interface PasswordEncoderPort {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
