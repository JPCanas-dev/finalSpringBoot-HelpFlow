package com.helpflow.support_ticket_service.application.port.in;

// Escala tickets que esten proximos a vencer segun el SLA
public interface EscalateTicketsPort {

    int checkAndEscalateNearBreachTickets();
}
