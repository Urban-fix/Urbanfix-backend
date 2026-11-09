package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetValidateDTO(
        @NotBlank(message = "El código es obligatorio")
        String code
) {}
