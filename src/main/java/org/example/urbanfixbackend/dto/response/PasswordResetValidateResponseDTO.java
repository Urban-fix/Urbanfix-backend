package org.example.urbanfixbackend.dto.response;

public record PasswordResetValidateResponseDTO(
        boolean valid,
        String message
) {}
