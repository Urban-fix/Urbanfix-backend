package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.urbanfixbackend.validation.PasswordConstraints;

public record PasswordResetConfirmDTO(
        @NotBlank(message = "El token es obligatorio")
        String token,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
        @Pattern(regexp = PasswordConstraints.PATTERN, message = PasswordConstraints.MESSAGE)
        String newPassword
) {}
