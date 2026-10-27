package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.mapper;

import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

// Mapper entre modelo de dominio User y UserJpaEntity
@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    User toDomain(UserJpaEntity entity);

    UserJpaEntity toEntity(User domain);
}
