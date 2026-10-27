package com.helpflow.support_ticket_service.infrastructure.adapter.persistence;

import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.TicketJpaEntity;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.entity.UserJpaEntity;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.mapper.TicketPersistenceMapper;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository.SpringDataTicketRepository;
import com.helpflow.support_ticket_service.infrastructure.adapter.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Adaptador que implementa el puerto TicketRepositoryPort
@Component
public class TicketPersistenceAdapter implements TicketRepositoryPort {

    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataUserRepository userRepository;
    private final TicketPersistenceMapper ticketMapper;

    public TicketPersistenceAdapter(SpringDataTicketRepository ticketRepository,
                                   SpringDataUserRepository userRepository,
                                   TicketPersistenceMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketJpaEntity entity = ticketMapper.toEntity(ticket);

        // Asociar referencias de usuario para integridad de JPA
        if (ticket.getRequesterId() != null) {
            UserJpaEntity req = userRepository.getReferenceById(ticket.getRequesterId());
            entity.setRequester(req);
        }
        if (ticket.getAssigneeId() != null) {
            UserJpaEntity ass = userRepository.getReferenceById(ticket.getAssigneeId());
            entity.setAssignee(ass);
        } else {
            entity.setAssignee(null);
        }

        TicketJpaEntity saved = ticketRepository.save(entity);
        return ticketMapper.toDomain(saved);
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id).map(ticketMapper::toDomain);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public List<Ticket> findByRequesterId(Long requesterId) {
        return ticketRepository.findByRequesterId(requesterId).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public List<Ticket> findByAssigneeId(Long assigneeId) {
        return ticketRepository.findByAssigneeId(assigneeId).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public long countActiveTicketsByAssigneeId(Long assigneeId) {
        return ticketRepository.countActiveTicketsByAssigneeId(assigneeId);
    }

    @Override
    public List<Ticket> findActiveTicketsNearDeadline(LocalDateTime threshold) {
        return ticketRepository.findActiveTicketsNearDeadline(threshold).stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public long countBreachedSlaTickets() {
        return ticketRepository.countBreachedSlaTickets();
    }

    @Override
    public long countByCategoryAndPriority(String category, String priority) {
        try {
            TicketCategory cat = TicketCategory.valueOf(category);
            TicketPriority pri = TicketPriority.valueOf(priority);
            return ticketRepository.countByCategoryAndPriority(cat, pri);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
}
