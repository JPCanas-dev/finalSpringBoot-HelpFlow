package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.CreateTicketPort.CreateTicketCommand;
import com.helpflow.support_ticket_service.application.port.out.TechnicianDispatchPort;
import com.helpflow.support_ticket_service.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.UserRepositoryPort;
import com.helpflow.support_ticket_service.domain.exception.UserInactiveException;
import com.helpflow.support_ticket_service.domain.model.Role;
import com.helpflow.support_ticket_service.domain.model.TechnicianAvailability;
import com.helpflow.support_ticket_service.domain.model.TechnicianDispatchSuggestion;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias para creacion y asignacion automatica de tickets
@ExtendWith(MockitoExtension.class)
class CreateTicketServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private TicketRepositoryPort ticketRepositoryPort;

    @Mock
    private TicketHistoryRepositoryPort ticketHistoryRepositoryPort;

    @Mock
    private TechnicianDispatchPort technicianDispatchPort;

    @Mock
    private TicketMetricsAdapter metricsAdapter;

    @InjectMocks
    private CreateTicketService createTicketService;

    private User requester;
    private User technician;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(10L);
        requester.setName("Maria Solicitante");
        requester.setStatus(UserStatus.ACTIVO);
        requester.setRole(Role.ROLE_USUARIO);

        technician = new User();
        technician.setId(20L);
        technician.setName("Carlos Tecnico");
        technician.setRole(Role.ROLE_TECNICO);
        technician.setDepartment("RED");
        technician.setLocation("SEDE_NORTE");
        technician.setStatus(UserStatus.ACTIVO);
        technician.setDocument("1020456789");
    }

    @Test
    void createTicket_ShouldAssignTechnician_WhenEligibleTechExists() {
        CreateTicketCommand command = new CreateTicketCommand(
                10L,
                "Falla en switch principal",
                "Sin conexion en piso 2",
                TicketCategory.RED,
                TicketPriority.ALTA,
                "SEDE_NORTE",
                "SWITCH-01"
        );

        Ticket savedTicket = new Ticket();
        savedTicket.setId(100L);
        savedTicket.setRequesterId(10L);
        savedTicket.setCategory(TicketCategory.RED);
        savedTicket.setPriority(TicketPriority.ALTA);
        savedTicket.setLocation("SEDE_NORTE");
        savedTicket.setStatus(TicketStatus.ABIERTO);

        when(userRepositoryPort.findById(10L)).thenReturn(Optional.of(requester));
        when(ticketRepositoryPort.save(any(Ticket.class))).thenReturn(savedTicket);

        TechnicianDispatchSuggestion suggestion = new TechnicianDispatchSuggestion(
                100L, "TEC-014", TechnicianAvailability.ALTA, 2
        );
        when(technicianDispatchPort.dispatchTechnician(any(), any(), any())).thenReturn(Optional.of(suggestion));
        when(userRepositoryPort.findByRole(Role.ROLE_TECNICO)).thenReturn(List.of(technician));
        when(ticketRepositoryPort.countActiveTicketsByAssigneeId(20L)).thenReturn(1L);

        Ticket result = createTicketService.createTicket(command);

        assertNotNull(result);
        assertEquals(TicketStatus.ASIGNADO, result.getStatus());
        assertEquals(20L, result.getAssigneeId());
        verify(ticketHistoryRepositoryPort, org.mockito.Mockito.atLeastOnce()).save(any(TicketHistory.class));
        verify(metricsAdapter).recordTicketCreated(TicketCategory.RED, TicketPriority.ALTA);
    }

    @Test
    void createTicket_ShouldThrowException_WhenRequesterIsInactive() {
        requester.setStatus(UserStatus.INACTIVO);
        CreateTicketCommand command = new CreateTicketCommand(
                10L, "Titulo", "Descripcion", TicketCategory.SOFTWARE, TicketPriority.MEDIA, "SEDE_CENTRAL", "PC"
        );

        when(userRepositoryPort.findById(10L)).thenReturn(Optional.of(requester));

        assertThrows(UserInactiveException.class, () -> createTicketService.createTicket(command));
    }
}
