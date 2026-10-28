package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.ManageTicketPort;
import com.helpflow.support_ticket_service.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.exception.BusinessRuleException;
import com.helpflow.support_ticket_service.domain.exception.EntityNotFoundException;
import com.helpflow.support_ticket_service.domain.exception.InvalidStateTransitionException;
import com.helpflow.support_ticket_service.domain.exception.UnauthorizedTicketAccessException;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.infrastructure.adapter.metrics.TicketMetricsAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

// Servicio para gestionar transiciones de estado y reasignaciones de tickets
@Service
public class ManageTicketService implements ManageTicketPort {

    private final TicketRepositoryPort ticketRepositoryPort;
    private final TicketHistoryRepositoryPort ticketHistoryRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TicketMetricsAdapter metricsAdapter;

    public ManageTicketService(TicketRepositoryPort ticketRepositoryPort,
                               TicketHistoryRepositoryPort ticketHistoryRepositoryPort,
                               UserRepositoryPort userRepositoryPort,
                               TicketMetricsAdapter metricsAdapter) {
        this.ticketRepositoryPort = ticketRepositoryPort;
        this.ticketHistoryRepositoryPort = ticketHistoryRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.metricsAdapter = metricsAdapter;
    }

    @Override
    @Transactional
    public Ticket updateStatus(UpdateTicketStatusCommand command) {
        Ticket ticket = ticketRepositoryPort.findById(command.ticketId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el ticket con ID: " + command.ticketId()));

        User currentUser = userRepositoryPort.findById(command.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario con ID: " + command.currentUserId()));

        // Validar permisos según rol
        try {
            validatePermissionsForStatusChange(currentUser, ticket, command.targetStatus());
        } catch (UnauthorizedTicketAccessException ex) {
            metricsAdapter.incrementAccessDenied();
            throw ex;
        }

        // Validar transición de estado de acuerdo a la máquina de estados
        if (!ticket.canTransitionTo(command.targetStatus())) {
            throw new InvalidStateTransitionException("Transición de estado no permitida: de " +
                    ticket.getStatus() + " a " + command.targetStatus());
        }

        TicketStatus previousStatus = ticket.getStatus();
        LocalDateTime now = LocalDateTime.now();

        // Si pasa a RESUELTO, registramos métrica de tiempo de resolución
        if (command.targetStatus() == TicketStatus.RESUELTO && ticket.getCreatedAt() != null) {
            Duration duration = Duration.between(ticket.getCreatedAt(), now);
            metricsAdapter.recordResolutionTime(duration);
        }

        // Si pasa a EN_PROGRESO desde ABIERTO/ASIGNADO, registramos métrica de primera respuesta
        if (command.targetStatus() == TicketStatus.EN_PROGRESO && ticket.getCreatedAt() != null) {
            Duration duration = Duration.between(ticket.getCreatedAt(), now);
            metricsAdapter.recordFirstResponseTime(duration);
        }

        ticket.setStatus(command.targetStatus());
        ticket = ticketRepositoryPort.save(ticket);

        // Registrar historial obligatorio con observaciones
        String observations = command.observations() != null && !command.observations().isBlank()
                ? command.observations()
                : "Cambio de estado a " + command.targetStatus();

        TicketHistory history = new TicketHistory(
                null,
                ticket.getId(),
                currentUser.getId(),
                previousStatus,
                command.targetStatus(),
                observations,
                now
        );
        ticketHistoryRepositoryPort.save(history);

        return ticket;
    }

    @Override
    @Transactional
    public Ticket reassignTicket(ReassignTicketCommand command) {
        User admin = userRepositoryPort.findById(command.adminUserId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario administrador"));

        // Solo administradores pueden reasignar tickets arbitrariamente
        if (!admin.isAdmin()) {
            metricsAdapter.incrementAccessDenied();
            throw new UnauthorizedTicketAccessException("Solo un usuario con rol ROLE_ADMIN puede reasignar tickets");
        }

        Ticket ticket = ticketRepositoryPort.findById(command.ticketId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el ticket con ID: " + command.ticketId()));

        User newTech = userRepositoryPort.findById(command.newAssigneeId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el técnico con ID: " + command.newAssigneeId()));

        if (!newTech.isTechnician()) {
            throw new BusinessRuleException("El usuario asignado debe tener el rol ROLE_TECNICO");
        }

        if (!newTech.isActive()) {
            throw new BusinessRuleException("No se puede asignar un ticket a un técnico inactivo");
        }

        ticket.setAssigneeId(newTech.getId());
        if (ticket.getStatus() == TicketStatus.ABIERTO) {
            ticket.setStatus(TicketStatus.ASIGNADO);
        }
        ticket = ticketRepositoryPort.save(ticket);

        String observations = command.observations() != null && !command.observations().isBlank()
                ? command.observations()
                : "Ticket reasignado al técnico: " + newTech.getName();

        TicketHistory history = new TicketHistory(
                null,
                ticket.getId(),
                admin.getId(),
                ticket.getStatus(),
                ticket.getStatus(),
                observations,
                LocalDateTime.now()
        );
        ticketHistoryRepositoryPort.save(history);

        return ticket;
    }

    private void validatePermissionsForStatusChange(User user, Ticket ticket, TicketStatus targetStatus) {
        // Admin tiene control total
        if (user.isAdmin()) {
            return;
        }

        // Técnico solo puede gestionar los tickets que tiene asignados
        if (user.isTechnician()) {
            if (ticket.getAssigneeId() == null || !ticket.getAssigneeId().equals(user.getId())) {
                throw new UnauthorizedTicketAccessException("El técnico solo puede gestionar tickets asignados a su cuenta");
            }
            return;
        }

        // Usuario solicitante solo puede cancelar su propio ticket o cerrarlo si ya está resuelto
        if (Role.ROLE_USUARIO.equals(user.getRole())) {
            if (!ticket.getRequesterId().equals(user.getId())) {
                throw new UnauthorizedTicketAccessException("El usuario solo puede gestionar sus propios tickets");
            }
            if (targetStatus != TicketStatus.CANCELADO && targetStatus != TicketStatus.CERRADO) {
                throw new UnauthorizedTicketAccessException("El usuario solo puede cancelar o dar cierre a su ticket");
            }
            return;
        }

        throw new UnauthorizedTicketAccessException("No tiene permisos para modificar este ticket");
    }
}
