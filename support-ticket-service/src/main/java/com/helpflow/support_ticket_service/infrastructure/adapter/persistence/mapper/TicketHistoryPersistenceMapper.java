package com.helpflow.support_ticket_service.infrastructure.adapter.persistence.mapper;

import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.TicketHistoryJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Mapper entre modelo de dominio TicketHistory y TicketHistoryJpaEntity
@Mapper(componentModel = "spring")
public interface TicketHistoryPersistenceMapper {

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "modifiedById", source = "modifiedBy.id")
    TicketHistory toDomain(TicketHistoryJpaEntity entity);

    @Mapping(target = "ticket.id", source = "ticketId")
    @Mapping(target = "modifiedBy.id", source = "modifiedById")
    TicketHistoryJpaEntity toEntity(TicketHistory domain);
}
