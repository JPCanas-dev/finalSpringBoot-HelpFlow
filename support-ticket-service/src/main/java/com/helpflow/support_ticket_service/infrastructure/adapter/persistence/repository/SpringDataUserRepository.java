package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository;

import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repositorio Spring Data JPA para usuarios
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByDocument(String document);

    List<UserJpaEntity> findByRole(Role role);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);
}
