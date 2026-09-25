package org.example.urbanfixbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.urbanfixbackend.validation.PasswordConstraints;
import org.example.urbanfixbackend.entity.enums.Rol;

public record RegisterRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 255, message = "El email no puede exceder 255 caracteres")
        String email,

        @NotBlank(message = "El password es obligatorio")
        @Size(min = 8, max = 255, message = "El password debe tener entre 8 y 255 caracteres")
        @Pattern(regexp = PasswordConstraints.PATTERN, message = PasswordConstraints.MESSAGE)
        String password,

        Rol rol
) {}
