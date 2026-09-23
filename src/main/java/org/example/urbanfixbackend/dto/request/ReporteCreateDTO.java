package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReporteCreateDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
        String descripcion,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "La latitud mínima es -90.0")
        @DecimalMax(value = "90.0", message = "La latitud máxima es 90.0")
        Double latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "La longitud mínima es -180.0")
        @DecimalMax(value = "180.0", message = "La longitud máxima es 180.0")
        Double longitud,

        @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
        String fotoUrl,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "La zona es obligatoria")
        Long zonaId
) {}
