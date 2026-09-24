package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.ComentarioCreateDTO;
import org.example.urbanfixbackend.dto.response.ComentarioResponseDTO;

import java.util.List;

public interface ComentarioService {

    ComentarioResponseDTO crearComentario(Long reporteId, ComentarioCreateDTO dto, Long usuarioId);

    List<ComentarioResponseDTO> getComentariosByReporteId(Long reporteId);

    void eliminarComentario(Long comentarioId, Long usuarioId);
}
