package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record ReporteUpdateDTO(
        @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
        String titulo,

        @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
        String descripcion,

        @DecimalMin(value = "-90.0", message = "La latitud mínima es -90.0")
        @DecimalMax(value = "90.0", message = "La latitud máxima es 90.0")
        Double latitud,

        @DecimalMin(value = "-180.0", message = "La longitud mínima es -180.0")
        @DecimalMax(value = "180.0", message = "La longitud máxima es 180.0")
        Double longitud,

        @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
        String fotoUrl
) {}
