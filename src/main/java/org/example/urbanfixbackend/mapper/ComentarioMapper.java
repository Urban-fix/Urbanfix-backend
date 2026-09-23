package org.example.urbanfixbackend.mapper;

import org.example.urbanfixbackend.dto.request.ComentarioCreateDTO;
import org.example.urbanfixbackend.dto.response.ComentarioResponseDTO;
import org.example.urbanfixbackend.entity.Comentario;

public class ComentarioMapper {

    public static ComentarioResponseDTO toDTO(Comentario comentario) {
        if (comentario == null) {
            return null;
        }
        return new ComentarioResponseDTO(
                comentario.getId(),
                comentario.getContenido(),
                comentario.getFechaCreacion(),
                UsuarioMapper.toDTO(comentario.getUsuario())
        );
    }

    public static Comentario toEntity(ComentarioCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        Comentario comentario = new Comentario();
        comentario.setContenido(dto.contenido());
        return comentario;
    }
}
