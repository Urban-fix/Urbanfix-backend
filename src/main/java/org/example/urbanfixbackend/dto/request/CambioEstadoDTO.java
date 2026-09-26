package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;

public record CambioEstadoDTO(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoReporte nuevoEstado
) {}
