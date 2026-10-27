package com.helpflow.support_ticket_service.application.port.out;

import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.User;

import java.util.List;
import java.util.Optional;

// Puerto de salida para acceso a datos de usuarios
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByDocument(String document);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);
}
