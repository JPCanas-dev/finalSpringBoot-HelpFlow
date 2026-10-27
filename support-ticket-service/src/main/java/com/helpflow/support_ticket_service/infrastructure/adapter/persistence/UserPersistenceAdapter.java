package com.helpflow.support_ticket_service.infrastructure.adapter.persistence;

import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.UserJpaEntity;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.mapper.UserPersistenceMapper;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// Adaptador que implementa el puerto UserRepositoryPort
@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository userRepository;
    private final UserPersistenceMapper userMapper;

    public UserPersistenceAdapter(SpringDataUserRepository userRepository,
                                  UserPersistenceMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toEntity(user);
        UserJpaEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByDocument(String document) {
        return userRepository.findByDocument(document).map(userMapper::toDomain);
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByDocument(String document) {
        return userRepository.existsByDocument(document);
    }
}
