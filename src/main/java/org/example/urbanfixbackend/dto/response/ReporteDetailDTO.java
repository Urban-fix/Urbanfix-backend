package org.example.urbanfixbackend.dto.response;

import org.example.urbanfixbackend.entity.enums.EstadoReporte;

import java.time.LocalDateTime;
import java.util.List;

public record ReporteDetailDTO(
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
        List<EstadoHistorialResponseDTO> historialEstados,
        List<ComentarioResponseDTO> comentarios,
        List<UsuarioResponseDTO> confirmaciones,
        long confirmacionesCount
) {}
