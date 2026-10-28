package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.CreateTicketPort;
import com.helpflow.support_ticket_service.application.port.out.TechnicianDispatchPort;
import com.helpflow.support_ticket_service.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.exception.BusinessRuleException;
import com.helpflow.support_ticket_service.domain.exception.EntityNotFoundException;
import com.helpflow.support_ticket_service.domain.exception.UserInactiveException;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.TechnicianDispatchSuggestion;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.infrastructure.adapter.metrics.TicketMetricsAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Servicio encargado de la creación y flujo inicial de asignación de tickets
@Service
public class CreateTicketService implements CreateTicketPort {

    private static final int MAX_ACTIVE_TICKETS_PER_TECH = 5;

    private final UserRepositoryPort userRepositoryPort;
    private final TicketRepositoryPort ticketRepositoryPort;
    private final TicketHistoryRepositoryPort ticketHistoryRepositoryPort;
    private final TechnicianDispatchPort technicianDispatchPort;
    private final TicketMetricsAdapter metricsAdapter;

    public CreateTicketService(UserRepositoryPort userRepositoryPort,
                               TicketRepositoryPort ticketRepositoryPort,
                               TicketHistoryRepositoryPort ticketHistoryRepositoryPort,
                               TechnicianDispatchPort technicianDispatchPort,
                               TicketMetricsAdapter metricsAdapter) {
        this.userRepositoryPort = userRepositoryPort;
        this.ticketRepositoryPort = ticketRepositoryPort;
        this.ticketHistoryRepositoryPort = ticketHistoryRepositoryPort;
        this.technicianDispatchPort = technicianDispatchPort;
        this.metricsAdapter = metricsAdapter;
    }

    @Override
    @Transactional
    public Ticket createTicket(CreateTicketCommand command) {
        // 1. Validar solicitante y que esté activo
        User requester = userRepositoryPort.findById(command.requesterId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario solicitante con ID: " + command.requesterId()));

        if (!requester.isActive()) {
            throw new UserInactiveException("El usuario solicitante no está ACTIVO para registrar tickets");
        }

        // 2. Crear ticket en estado ABIERTO con fecha límite de SLA según prioridad
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime slaDeadline = Ticket.calculateSlaDeadline(now, command.priority());

        Ticket ticket = new Ticket();
        ticket.setRequesterId(requester.getId());
        ticket.setTitle(command.title());
        ticket.setDescription(command.description());
        ticket.setCategory(command.category());
        ticket.setPriority(command.priority());
        ticket.setLocation(command.location());
        ticket.setAffectedAsset(command.affectedAsset());
        ticket.setStatus(TicketStatus.ABIERTO);
        ticket.setCreatedAt(now);
        ticket.setSlaDeadline(slaDeadline);

        // Guardamos primero para tener el ID generado
        ticket = ticketRepositoryPort.save(ticket);

        // Registrar métrica de ticket creado
        metricsAdapter.recordTicketCreated(ticket.getCategory(), ticket.getPriority());

        // Registramos historial inicial de apertura
        TicketHistory openHistory = new TicketHistory(
                null,
                ticket.getId(),
                requester.getId(),
                null,
                TicketStatus.ABIERTO,
                "Ticket creado por el usuario en estado ABIERTO",
                now
        );
        ticketHistoryRepositoryPort.save(openHistory);

        // 3. Consultar servicio externo simulado de disponibilidad técnica
        Optional<TechnicianDispatchSuggestion> suggestionOpt =
                technicianDispatchPort.dispatchTechnician(ticket.getId(), ticket.getCategory(), ticket.getPriority());

        // 4. Aplicar reglas internas de asignación
        User assignedTech = selectBestTechnician(ticket, suggestionOpt.orElse(null));

        if (assignedTech != null) {
            ticket.setAssigneeId(assignedTech.getId());
            ticket.setStatus(TicketStatus.ASIGNADO);
            ticket = ticketRepositoryPort.save(ticket);

            String obs = "Ticket asignado automáticamente al técnico: " + assignedTech.getName();
            if (suggestionOpt.isPresent()) {
                obs += " [Sugerencia: " + suggestionOpt.get().tecnicoId() + ", Horas estimadas: " +
                        suggestionOpt.get().tiempoEstimadoHoras() + ", Disponibilidad: " +
                        suggestionOpt.get().disponibilidad() + "]";
            }

            TicketHistory assignHistory = new TicketHistory(
                    null,
                    ticket.getId(),
                    requester.getId(),
                    TicketStatus.ABIERTO,
                    TicketStatus.ASIGNADO,
                    obs,
                    LocalDateTime.now()
            );
            ticketHistoryRepositoryPort.save(assignHistory);
        }

        return ticket;
    }

    // Reglas de asignación: especialidad/departamento = categoría, misma sede y carga menor a 5
    private User selectBestTechnician(Ticket ticket, TechnicianDispatchSuggestion suggestion) {
        List<User> technicians = userRepositoryPort.findByRole(Role.ROLE_TECNICO);
        if (technicians.isEmpty()) {
            return null;
        }

        // Filtramos solo técnicos activos
        List<User> activeTechs = technicians.stream()
                .filter(User::isActive)
                .toList();

        // 1. Intentar con técnico sugerido por el dispatch si cumple reglas
        if (suggestion != null && suggestion.tecnicoId() != null) {
            Optional<User> suggestedMatch = activeTechs.stream()
                    .filter(t -> t.getDocument().endsWith(extractDigits(suggestion.tecnicoId())) ||
                                 t.getName().toLowerCase().contains(suggestion.tecnicoId().toLowerCase()))
                    .filter(t -> isEligible(t, ticket))
                    .findFirst();

            if (suggestedMatch.isPresent()) {
                return suggestedMatch.get();
            }
        }

        // 2. Buscar técnico de la misma sede y misma especialidad (departamento == categoría)
        Optional<User> bestLocal = activeTechs.stream()
                .filter(t -> matchesCategory(t, ticket.getCategory()))
                .filter(t -> matchesLocation(t, ticket.getLocation()))
                .filter(t -> hasCapacity(t.getId()))
                .findFirst();

        if (bestLocal.isPresent()) {
            return bestLocal.get();
        }

        // 3. Buscar técnico de misma especialidad en cualquier sede con capacidad
        Optional<User> anyBySpecialty = activeTechs.stream()
                .filter(t -> matchesCategory(t, ticket.getCategory()))
                .filter(t -> hasCapacity(t.getId()))
                .findFirst();

        if (anyBySpecialty.isPresent()) {
            return anyBySpecialty.get();
        }

        // 4. Cualquier técnico disponible con capacidad
        return activeTechs.stream()
                .filter(t -> hasCapacity(t.getId()))
                .findFirst()
                .orElse(null);
    }

    private boolean isEligible(User tech, Ticket ticket) {
        return matchesCategory(tech, ticket.getCategory()) && hasCapacity(tech.getId());
    }

    private boolean matchesCategory(User tech, com.helpflow.support_ticket_service.domain.model.TicketCategory category) {
        if (tech.getDepartment() == null || category == null) {
            return false;
        }
        return tech.getDepartment().equalsIgnoreCase(category.name());
    }

    private boolean matchesLocation(User tech, String location) {
        if (tech.getLocation() == null || location == null) {
            return false;
        }
        return tech.getLocation().equalsIgnoreCase(location);
    }

    private boolean hasCapacity(Long techId) {
        long currentTickets = ticketRepositoryPort.countActiveTicketsByAssigneeId(techId);
        return currentTickets < MAX_ACTIVE_TICKETS_PER_TECH;
    }

    private String extractDigits(String str) {
        return str.replaceAll("\\D+", "");
    }
}
