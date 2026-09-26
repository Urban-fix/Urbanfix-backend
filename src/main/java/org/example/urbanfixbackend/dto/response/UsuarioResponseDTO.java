package org.example.urbanfixbackend.dto.response;

import org.example.urbanfixbackend.entity.enums.Rol;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        Rol rol,
        LocalDateTime fechaRegistro
) {}
