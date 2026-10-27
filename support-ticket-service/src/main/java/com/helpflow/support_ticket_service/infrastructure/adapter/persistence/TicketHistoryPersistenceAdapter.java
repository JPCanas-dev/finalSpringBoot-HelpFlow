package com.helpflow.support_ticket_service.infrastructure.adapter.persistence;

import com.helpflow.support_ticket_service.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.TicketHistoryJpaEntity;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.TicketJpaEntity;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.UserJpaEntity;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.mapper.TicketHistoryPersistenceMapper;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository.SpringDataTicketHistoryRepository;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository.SpringDataTicketRepository;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

// Adaptador que implementa el puerto TicketHistoryRepositoryPort
@Component
public class TicketHistoryPersistenceAdapter implements TicketHistoryRepositoryPort {

    private final SpringDataTicketHistoryRepository historyRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataUserRepository userRepository;
    private final TicketHistoryPersistenceMapper historyMapper;

    public TicketHistoryPersistenceAdapter(SpringDataTicketHistoryRepository historyRepository,
                                          SpringDataTicketRepository ticketRepository,
                                          SpringDataUserRepository userRepository,
                                          TicketHistoryPersistenceMapper historyMapper) {
        this.historyRepository = historyRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.historyMapper = historyMapper;
    }

    @Override
    public TicketHistory save(TicketHistory history) {
        TicketHistoryJpaEntity entity = historyMapper.toEntity(history);

        // Asociar referencias
        if (history.getTicketId() != null) {
            TicketJpaEntity ticket = ticketRepository.getReferenceById(history.getTicketId());
            entity.setTicket(ticket);
        }
        if (history.getModifiedById() != null) {
            UserJpaEntity user = userRepository.getReferenceById(history.getModifiedById());
            entity.setModifiedBy(user);
        }

        TicketHistoryJpaEntity saved = historyRepository.save(entity);
        return historyMapper.toDomain(saved);
    }

    @Override
    public List<TicketHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId) {
        return historyRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(historyMapper::toDomain)
                .toList();
    }
}
