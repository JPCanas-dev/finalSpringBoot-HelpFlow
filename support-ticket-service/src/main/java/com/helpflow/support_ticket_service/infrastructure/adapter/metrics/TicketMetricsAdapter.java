package com.helpflow.support_ticket_service.infrastructure.adapter.metrics;

import com.helpflow.support_ticket_service.application.port.out.TicketRepositoryPort;
import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

// Metricas personalizadas de negocio con Micrometer
@Component
public class TicketMetricsAdapter {

    private final MeterRegistry meterRegistry;
    private final Counter authFailuresCounter;
    private final Counter accessDeniedCounter;
    private final Timer resolutionTimer;
    private final Timer firstResponseTimer;

    public TicketMetricsAdapter(MeterRegistry meterRegistry, TicketRepositoryPort ticketRepositoryPort) {
        this.meterRegistry = meterRegistry;

        // Contador de autenticaciones fallidas
        this.authFailuresCounter = Counter.builder("helpflow_auth_failures_total")
                .description("Total de fallas de autenticacion en el sistema")
                .register(meterRegistry);

        // Contador de accesos denegados
        this.accessDeniedCounter = Counter.builder("helpflow_access_denied_total")
                .description("Total de accesos denegados a recursos de soporte")
                .register(meterRegistry);

        // Temporizador para primera respuesta
        this.firstResponseTimer = Timer.builder("helpflow_ticket_first_response_time_seconds")
                .description("Tiempo transcurrido hasta la primera respuesta o asignacion del ticket")
                .register(meterRegistry);

        // Temporizador para resolucion de tickets
        this.resolutionTimer = Timer.builder("helpflow_ticket_resolution_time_seconds")
                .description("Tiempo total transcurrido hasta la resolucion del ticket")
                .register(meterRegistry);

        // Gauge para contar tickets que incumplieron SLA
        Gauge.builder("helpflow_tickets_breached_sla_total", ticketRepositoryPort, TicketRepositoryPort::countBreachedSlaTickets)
                .description("Cantidad de tickets activos que han superado el limite de SLA")
                .register(meterRegistry);
    }

    // Incrementa contador de tickets creados por categoria y prioridad
    public void recordTicketCreated(TicketCategory category, TicketPriority priority) {
        Counter.builder("helpflow_tickets_created_total")
                .description("Tickets creados por categoria y prioridad")
                .tag("category", category != null ? category.name() : "DESCONOCIDA")
                .tag("priority", priority != null ? priority.name() : "DESCONOCIDA")
                .register(meterRegistry)
                .increment();
    }

    public void incrementAuthFailure() {
        authFailuresCounter.increment();
    }

    public void incrementAccessDenied() {
        accessDeniedCounter.increment();
    }

    public void recordResolutionTime(Duration duration) {
        resolutionTimer.record(duration);
    }

    public void recordFirstResponseTime(Duration duration) {
        firstResponseTimer.record(duration);
    }
}
