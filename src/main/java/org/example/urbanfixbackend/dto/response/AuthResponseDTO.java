package org.example.urbanfixbackend.dto.response;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        UsuarioResponseDTO usuario
) {}
