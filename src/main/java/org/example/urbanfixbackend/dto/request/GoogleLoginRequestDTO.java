package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.NotBlank;

/** idToken que devuelve "Sign in with Google" en el frontend web o en la app móvil. */
public record GoogleLoginRequestDTO(
        @NotBlank(message = "El token de Google es obligatorio")
        String idToken
) {}
