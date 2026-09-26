package org.example.urbanfixbackend.dto.response;

import org.example.urbanfixbackend.entity.enums.EstadoReporte;

import java.time.LocalDateTime;

public record ReporteResponseDTO(
        Long id,
        String titulo,
        String descripcion,
        Double latitud,
        Double longitud,
        String fotoUrl,
        LocalDateTime fechaCreacion,
        EstadoReporte estadoActual,
        UsuarioResponseDTO usuario,
        CategoriaResponseDTO categoria,
        ZonaResponseDTO zona,
        long confirmacionesCount
) {}
