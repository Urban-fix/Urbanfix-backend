package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioCreateDTO(
        @NotBlank(message = "El contenido del comentario es obligatorio")
        @Size(min = 1, max = 1000, message = "El comentario debe tener entre 1 y 1000 caracteres")
        String contenido
) {}
