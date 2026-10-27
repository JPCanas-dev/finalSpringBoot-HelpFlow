package com.helpflow.technician_dispatch_service.service;

import com.helpflow.technician_dispatch_service.dto.DispatchDto.DispatchRequest;
import com.helpflow.technician_dispatch_service.dto.DispatchDto.DispatchResponse;
import org.springframework.stereotype.Service;

import java.util.Random;

// Servicio con logica determinista segun seed numerico
@Service
public class DispatchService {

    // Lista de tecnicos simulados
    private static final String[] TECHNICIANS = {
            "TEC-014", "TEC-021", "TEC-008", "TEC-035", "TEC-019", "TEC-042"
    };

    public DispatchResponse dispatch(DispatchRequest request) {
        Long ticketId = request.ticketId() != null ? request.ticketId() : 1L;
        String categoria = request.categoria() != null ? request.categoria() : "OTRO";
        String prioridad = request.prioridad() != null ? request.prioridad() : "MEDIA";

        // 1. Calculamos seed numerico determinista
        long seed = (long) (ticketId.hashCode() * 31 + categoria.hashCode()) * 31 + prioridad.hashCode();
        Random random = new Random(seed);

        // 2. Seleccionar tecnico determinista
        int techIndex = Math.abs(random.nextInt()) % TECHNICIANS.length;
        String selectedTech = TECHNICIANS[techIndex];

        // 3. Generar tiempo estimado entre 1 y 12 horas
        int tiempoEstimadoHoras = 1 + (Math.abs(random.nextInt()) % 12);

        // 4. Clasificar disponibilidad segun enunciado:
        // 0-2 horas -> DISPONIBILIDAD ALTA
        // 3-8 horas -> DISPONIBILIDAD MEDIA
        // Mas de 8 horas -> DISPONIBILIDAD BAJA
        String disponibilidad;
        if (tiempoEstimadoHoras <= 2) {
            disponibilidad = "ALTA";
        } else if (tiempoEstimadoHoras <= 8) {
            disponibilidad = "MEDIA";
        } else {
            disponibilidad = "BAJA";
        }

        return new DispatchResponse(
                ticketId,
                selectedTech,
                disponibilidad,
                tiempoEstimadoHoras
        );
    }
}
