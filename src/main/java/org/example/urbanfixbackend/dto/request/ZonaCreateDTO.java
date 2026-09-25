package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ZonaCreateDTO(
        @NotBlank(message = "El nombre de la zona es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Size(max = 255, message = "Las coordenadas no pueden exceder 255 caracteres")
        String coordenadasReferencia
) {}
