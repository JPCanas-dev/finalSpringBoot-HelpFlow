package com.helpflow.support_ticket_service.infrastructure.adapter.client;

import com.helpflow.support_ticket_service.application.port.out.TechnicianDispatchPort;
import com.helpflow.support_ticket_service.domain.model.TechnicianAvailability;
import com.helpflow.support_ticket_service.domain.model.TechnicianDispatchSuggestion;
import com.helpflow.support_ticket_service.domain.model.TicketCategory;
import com.helpflow.support_ticket_service.domain.model.TicketPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

// Adaptador REST para comunicarse con el microservicio technician-dispatch-service
@Component
public class TechnicianDispatchRestAdapter implements TechnicianDispatchPort {

    private static final Logger log = LoggerFactory.getLogger(TechnicianDispatchRestAdapter.class);

    private final RestClient restClient;

    public TechnicianDispatchRestAdapter(@Value("${technician.dispatch.service.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // DTO interno para enviar la petición
    record DispatchRequestDto(Long ticketId, String categoria, String prioridad) {}

    // DTO interno para recibir la respuesta
    record DispatchResponseDto(Long ticketId, String tecnicoId, String disponibilidad, Integer tiempoEstimadoHoras) {}

    @Override
    public Optional<TechnicianDispatchSuggestion> dispatchTechnician(Long ticketId, TicketCategory category, TicketPriority priority) {
        try {
            DispatchRequestDto request = new DispatchRequestDto(
                    ticketId,
                    category != null ? category.name() : "OTRO",
                    priority != null ? priority.name() : "MEDIA"
            );

            DispatchResponseDto response = restClient.post()
                    .uri("/technician-dispatch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(DispatchResponseDto.class);

            if (response == null) {
                return Optional.empty();
            }

            TechnicianAvailability availability = parseAvailability(response.disponibilidad());

            TechnicianDispatchSuggestion suggestion = new TechnicianDispatchSuggestion(
                    response.ticketId(),
                    response.tecnicoId(),
                    availability,
                    response.tiempoEstimadoHoras()
            );

            return Optional.of(suggestion);

        } catch (Exception e) {
            // Si el servicio no responde o falla, no bloqueamos la creación del ticket
            log.warn("No fue posible comunicarse con technician-dispatch-service: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private TechnicianAvailability parseAvailability(String text) {
        if (text == null) {
            return TechnicianAvailability.MEDIA;
        }
        try {
            return TechnicianAvailability.valueOf(text.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return TechnicianAvailability.MEDIA;
        }
    }
}
