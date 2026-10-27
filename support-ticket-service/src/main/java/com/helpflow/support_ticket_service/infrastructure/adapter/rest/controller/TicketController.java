package com.helpflow.support_ticket_service.infrastructure.adapter.rest.controller;

import com.helpflow.support_ticket_service.application.port.in.CreateTicketPort;
import com.helpflow.support_ticket_service.application.port.in.CreateTicketPort.CreateTicketCommand;
import com.helpflow.support_ticket_service.application.port.in.EscalateTicketsPort;
import com.helpflow.support_ticket_service.application.port.in.GetTicketPort;
import com.helpflow.support_ticket_service.application.port.in.ManageTicketPort;
import com.helpflow.support_ticket_service.application.port.in.ManageTicketPort.ReassignTicketCommand;
import com.helpflow.support_ticket_service.application.port.in.ManageTicketPort.UpdateTicketStatusCommand;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.TicketDto.CreateTicketRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.TicketDto.ReassignTicketRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.TicketDto.TicketHistoryResponse;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.TicketDto.TicketResponse;
import com.helpflow.support_ticket_service.infrastructure.adapter.rest.dto.TicketDto.UpdateStatusRequest;
import com.helpflow.support_ticket_service.infrastructure.adapter.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// Controlador para la gestion integral de tickets
@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "Endpoints para crear, consultar, asignar y escalar tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final CreateTicketPort createTicketPort;
    private final ManageTicketPort manageTicketPort;
    private final GetTicketPort getTicketPort;
    private final EscalateTicketsPort escalateTicketsPort;

    public TicketController(CreateTicketPort createTicketPort,
                            ManageTicketPort manageTicketPort,
                            GetTicketPort getTicketPort,
                            EscalateTicketsPort escalateTicketsPort) {
        this.createTicketPort = createTicketPort;
        this.manageTicketPort = manageTicketPort;
        this.getTicketPort = getTicketPort;
        this.escalateTicketsPort = escalateTicketsPort;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMIN')")
    @Operation(summary = "Crear nuevo ticket y ejecutar asignacion automatica")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {

        CreateTicketCommand command = new CreateTicketCommand(
                currentUser.getId(),
                request.title(),
                request.description(),
                request.category(),
                request.priority(),
                request.location(),
                request.affectedAsset()
        );

        Ticket ticket = createTicketPort.createTicket(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ticket));
    }

    @GetMapping
    @Operation(summary = "Listar tickets permitidos segun el usuario autenticado")
    public ResponseEntity<List<TicketResponse>> getTickets(@AuthenticationPrincipal SecurityUser currentUser) {
        List<Ticket> tickets = getTicketPort.getTicketsForUser(currentUser.getId());
        List<TicketResponse> responses = tickets.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar detalle de un ticket por ID")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        Ticket ticket = getTicketPort.getTicketById(id, currentUser.getId());
        return ResponseEntity.ok(toResponse(ticket));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de un ticket con observaciones")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {

        UpdateTicketStatusCommand command = new UpdateTicketStatusCommand(
                id,
                currentUser.getId(),
                request.targetStatus(),
                request.observations()
        );

        Ticket updated = manageTicketPort.updateStatus(command);
        return ResponseEntity.ok(toResponse(updated));
    }

    @PutMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reasignar ticket a otro tecnico (solo administradores)")
    public ResponseEntity<TicketResponse> reassignTicket(
            @PathVariable Long id,
            @Valid @RequestBody ReassignTicketRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {

        ReassignTicketCommand command = new ReassignTicketCommand(
                id,
                currentUser.getId(),
                request.newAssigneeId(),
                request.observations()
        );

        Ticket updated = manageTicketPort.reassignTicket(command);
        return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Consultar el historial de auditoria de un ticket")
    public ResponseEntity<List<TicketHistoryResponse>> getHistories(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        List<TicketHistory> histories = getTicketPort.getHistoriesForTicket(id, currentUser.getId());
        List<TicketHistoryResponse> responses = histories.stream()
                .map(this::toHistoryResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/escalate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Escalar manualmente tickets proximos a incumplir SLA")
    public ResponseEntity<Map<String, Object>> escalateTickets() {
        int escalated = escalateTicketsPort.checkAndEscalateNearBreachTickets();
        return ResponseEntity.ok(Map.of(
                "message", "Proceso de escalamiento ejecutado exitosamente",
                "ticketsEscalados", escalated
        ));
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getRequesterId(),
                t.getAssigneeId(),
                t.getTitle(),
                t.getDescription(),
                t.getCategory(),
                t.getPriority(),
                t.getLocation(),
                t.getAffectedAsset(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getSlaDeadline(),
                t.isSlaBreached()
        );
    }

    private TicketHistoryResponse toHistoryResponse(TicketHistory h) {
        return new TicketHistoryResponse(
                h.getId(),
                h.getTicketId(),
                h.getModifiedById(),
                h.getPreviousStatus(),
                h.getNewStatus(),
                h.getObservations(),
                h.getCreatedAt()
        );
    }
}
