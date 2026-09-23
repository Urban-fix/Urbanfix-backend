package org.example.urbanfixbackend.dto.response;

import java.time.LocalDateTime;

public record ComentarioResponseDTO(
        Long id,
        String contenido,
        LocalDateTime fechaCreacion,
        UsuarioResponseDTO usuario
) {}
