package org.example.urbanfixbackend.dto.response;

import org.example.urbanfixbackend.entity.enums.EstadoReporte;

import java.time.LocalDateTime;

public record EstadoHistorialResponseDTO(
        Long id,
        EstadoReporte estadoAnterior,
        EstadoReporte estadoNuevo,
        LocalDateTime fechaCambio,
        UsuarioResponseDTO usuario
) {}
