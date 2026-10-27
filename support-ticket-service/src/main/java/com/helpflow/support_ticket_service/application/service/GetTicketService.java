package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.GetTicketPort;
import com.helpflow.support_ticket_service.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.exception.EntityNotFoundException;
import com.helpflow.support_ticket_service.domain.exception.UnauthorizedTicketAccessException;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Servicio para consultar tickets e historiales con restricciones por propietario
@Service
public class GetTicketService implements GetTicketPort {

    private final TicketRepositoryPort ticketRepositoryPort;
    private final TicketHistoryRepositoryPort ticketHistoryRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public GetTicketService(TicketRepositoryPort ticketRepositoryPort,
                            TicketHistoryRepositoryPort ticketHistoryRepositoryPort,
                            UserRepositoryPort userRepositoryPort) {
        this.ticketRepositoryPort = ticketRepositoryPort;
        this.ticketHistoryRepositoryPort = ticketHistoryRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketById(Long ticketId, Long currentUserId) {
        User user = getUser(currentUserId);
        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro el ticket con ID: " + ticketId));

        checkReadPermission(user, ticket);
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsForUser(Long currentUserId) {
        User user = getUser(currentUserId);

        // Admin ve todos los tickets
        if (user.isAdmin()) {
            return ticketRepositoryPort.findAll();
        }

        // Tecnico ve sus tickets asignados
        if (user.isTechnician()) {
            return ticketRepositoryPort.findByAssigneeId(user.getId());
        }

        // Usuario solicitante ve solo los suyos
        return ticketRepositoryPort.findByRequesterId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketHistory> getHistoriesForTicket(Long ticketId, Long currentUserId) {
        User user = getUser(currentUserId);
        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro el ticket con ID: " + ticketId));

        checkReadPermission(user, ticket);
        return ticketHistoryRepositoryPort.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    private User getUser(Long userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    private void checkReadPermission(User user, Ticket ticket) {
        if (user.isAdmin()) {
            return;
        }

        if (user.isTechnician() && ticket.getAssigneeId() != null && ticket.getAssigneeId().equals(user.getId())) {
            return;
        }

        if (Role.ROLE_USUARIO.equals(user.getRole()) && ticket.getRequesterId().equals(user.getId())) {
            return;
        }

        throw new UnauthorizedTicketAccessException("No tiene autorizacion para consultar este ticket");
    }
}
