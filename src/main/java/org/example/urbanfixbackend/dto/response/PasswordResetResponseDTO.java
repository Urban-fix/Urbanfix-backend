package org.example.urbanfixbackend.dto.response;

public record PasswordResetResponseDTO(
        String message,
        String token
) {
}
