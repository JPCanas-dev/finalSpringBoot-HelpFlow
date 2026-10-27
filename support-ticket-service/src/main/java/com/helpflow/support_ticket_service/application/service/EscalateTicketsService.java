package com.helpflow.support_ticket_service.application.service;

import com.helpflow.support_ticket_service.application.port.in.EscalateTicketsPort;
import com.helpflow.support_ticket_service.application.port.out.TicketHistoryRepositoryPort;
import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.domain.model.Ticket;
import com.helpflow.support_ticket_service.domain.model.TicketHistory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Servicio para escalar de forma automatica tickets en riesgo de incumplir el SLA
@Service
public class EscalateTicketsService implements EscalateTicketsPort {

    private static final int WARNING_HOURS_BEFORE_BREACH = 2;

    private final TicketRepositoryPort ticketRepositoryPort;
    private final TicketHistoryRepositoryPort ticketHistoryRepositoryPort;

    public EscalateTicketsService(TicketRepositoryPort ticketRepositoryPort,
                                  TicketHistoryRepositoryPort ticketHistoryRepositoryPort) {
        this.ticketRepositoryPort = ticketRepositoryPort;
        this.ticketHistoryRepositoryPort = ticketHistoryRepositoryPort;
    }

    @Override
    @Transactional
    public int checkAndEscalateNearBreachTickets() {
        LocalDateTime threshold = LocalDateTime.now().plusHours(WARNING_HOURS_BEFORE_BREACH);
        List<Ticket> nearBreachTickets = ticketRepositoryPort.findActiveTicketsNearDeadline(threshold);

        int escalatedCount = 0;

        for (Ticket ticket : nearBreachTickets) {
            // Si la prioridad no es ya CRITICA, se escala a CRITICA
            if (ticket.getPriority() != TicketPriority.CRITICA) {
                TicketPriority previousPriority = ticket.getPriority();
                ticket.setPriority(TicketPriority.CRITICA);
                ticketRepositoryPort.save(ticket);

                // Registramos auditoria del escalamiento
                TicketHistory history = new TicketHistory(
                        null,
                        ticket.getId(),
                        ticket.getRequesterId(),
                        ticket.getStatus(),
                        ticket.getStatus(),
                        "Escalamiento automatico de SLA: prioridad cambio de " + previousPriority +
                                " a CRITICA por proximidad de vencimiento",
                        LocalDateTime.now()
                );
                ticketHistoryRepositoryPort.save(history);
                escalatedCount++;
            }
        }

        return escalatedCount;
    }
}
