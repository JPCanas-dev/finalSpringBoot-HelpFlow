package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.ManageTicketPort.ReassignTicketCommand;
import com.helpflow.support_ticket_service.application.port.in.ManageTicketPort.UpdateTicketStatusCommand;
import com.helpflow.support_ticket_service.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.exception.InvalidStateTransitionException;
import com.helpflow.support_ticket_service.domain.exception.UnauthorizedTicketAccessException;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.domain.model.TicketStatus;
import com.helpflow.support_ticket_service.domain.model.User;
import com.helpflow.support_ticket_service.domain.model.UserStatus;
import com.helpflow.support_ticket_service.infrastructure.adapter.metrics.TicketMetricsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias para transiciones de estado y reasignación de tickets
@ExtendWith(MockitoExtension.class)
class ManageTicketServiceTest {

    @Mock
    private TicketRepositoryPort ticketRepositoryPort;

    @Mock
    private TicketHistoryRepositoryPort ticketHistoryRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private TicketMetricsAdapter metricsAdapter;

    @InjectMocks
    private ManageTicketService manageTicketService;

    private User technician;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        technician = new User();
        technician.setId(5L);
        technician.setName("Tecnico Asignado");
        technician.setRole(Role.ROLE_TECNICO);
        technician.setStatus(UserStatus.ACTIVO);

        ticket = new Ticket();
        ticket.setId(50L);
        ticket.setRequesterId(1L);
        ticket.setAssigneeId(5L);
        ticket.setStatus(TicketStatus.ASIGNADO);
        ticket.setCreatedAt(LocalDateTime.now().minusHours(1));
    }

    // CASO FELIZ: Transición válida a EN_PROGRESO cuando el técnico asignado realiza la actualización
    @Test
    void updateStatus_ShouldAdvanceToInProgress_WhenTechnicianIsOwner() {
        UpdateTicketStatusCommand command = new UpdateTicketStatusCommand(
                50L, 5L, TicketStatus.EN_PROGRESO, "Inicio revision tecnica"
        );

        when(ticketRepositoryPort.findById(50L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(5L)).thenReturn(Optional.of(technician));
        when(ticketRepositoryPort.save(any(Ticket.class))).thenReturn(ticket);

        Ticket updated = manageTicketService.updateStatus(command);

        assertNotNull(updated);
        assertEquals(TicketStatus.EN_PROGRESO, updated.getStatus());
        verify(ticketHistoryRepositoryPort).save(any(TicketHistory.class));
    }

    // CASO DE ERROR / LÍMITE: Transición inválida de estados (de ASIGNADO no se puede saltar directamente a CERRADO)
    @Test
    void updateStatus_ShouldThrowException_WhenInvalidTransitionAttempted() {
        // De ASIGNADO no se puede saltar directamente a CERRADO
        UpdateTicketStatusCommand command = new UpdateTicketStatusCommand(
                50L, 5L, TicketStatus.CERRADO, "Cierre directo no permitido"
        );

        when(ticketRepositoryPort.findById(50L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(5L)).thenReturn(Optional.of(technician));

        assertThrows(InvalidStateTransitionException.class, () -> manageTicketService.updateStatus(command));
    }

    // CASO DE ERROR / SEGURIDAD: Bloquear acceso si un técnico intenta gestionar un ticket que no tiene asignado
    @Test
    void updateStatus_ShouldThrowException_WhenTechnicianIsNotAssigned() {
        User otherTech = new User();
        otherTech.setId(99L);
        otherTech.setRole(Role.ROLE_TECNICO);

        UpdateTicketStatusCommand command = new UpdateTicketStatusCommand(
                50L, 99L, TicketStatus.EN_PROGRESO, "Intento ajeno"
        );

        when(ticketRepositoryPort.findById(50L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.of(otherTech));

        assertThrows(UnauthorizedTicketAccessException.class, () -> manageTicketService.updateStatus(command));
        verify(metricsAdapter).incrementAccessDenied();
    }

    // CASO FELIZ: Reasignación de ticket permitida cuando la solicitud proviene de un Administrador (ROLE_ADMIN)
    @Test
    void reassignTicket_ShouldUpdateAssignee_WhenAdminRequests() {
        User admin = new User();
        admin.setId(2L);
        admin.setRole(Role.ROLE_ADMIN);

        User newTech = new User();
        newTech.setId(8L);
        newTech.setName("Nuevo Tecnico");
        newTech.setRole(Role.ROLE_TECNICO);
        newTech.setStatus(UserStatus.ACTIVO);

        ReassignTicketCommand command = new ReassignTicketCommand(50L, 2L, 8L, "Reasignacion por carga");

        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(admin));
        when(ticketRepositoryPort.findById(50L)).thenReturn(Optional.of(ticket));
        when(userRepositoryPort.findById(8L)).thenReturn(Optional.of(newTech));
        when(ticketRepositoryPort.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = manageTicketService.reassignTicket(command);

        assertNotNull(result);
        assertEquals(8L, result.getAssigneeId());
        verify(ticketHistoryRepositoryPort).save(any(TicketHistory.class));
    }
}
