package com.helpflow.technician_dispatch_service.dto;

// DTOs para peticion y respuesta del despacho de tecnicos
public class DispatchDto {

    public record DispatchRequest(
            Long ticketId,
            String categoria,
            String prioridad
    ) {}

    public record DispatchResponse(
            Long ticketId,
            String tecnicoId,
            String disponibilidad,
            Integer tiempoEstimadoHoras
    ) {}
}
