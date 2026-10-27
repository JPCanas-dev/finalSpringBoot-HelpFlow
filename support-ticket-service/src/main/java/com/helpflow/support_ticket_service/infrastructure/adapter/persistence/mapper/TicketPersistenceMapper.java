package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.mapper;

import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.TicketJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Mapper entre modelo de dominio Ticket y TicketJpaEntity
@Mapper(componentModel = "spring")
public interface TicketPersistenceMapper {

    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    Ticket toDomain(TicketJpaEntity entity);

    @Mapping(target = "requester.id", source = "requesterId")
    @Mapping(target = "assignee.id", source = "assigneeId")
    TicketJpaEntity toEntity(Ticket domain);
}
