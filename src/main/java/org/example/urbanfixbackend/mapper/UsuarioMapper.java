package org.example.urbanfixbackend.mapper;

import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.response.UsuarioResponseDTO;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.Rol;

public class UsuarioMapper {

    public static UsuarioResponseDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getFechaRegistro()
        );
    }

    public static Usuario toEntity(RegisterRequestDTO dto, Rol rol) {
        if (dto == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setEmail(dto.email());
        usuario.setPassword(dto.password());
        usuario.setRol(rol);
        return usuario;
    }

    public static Usuario toEntity(RegisterRequestDTO dto) {
        return toEntity(dto, Rol.CIUDADANO);
    }
}
