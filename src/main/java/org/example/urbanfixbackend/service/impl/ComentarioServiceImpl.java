package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.ComentarioCreateDTO;
import org.example.urbanfixbackend.dto.response.ComentarioResponseDTO;
import org.example.urbanfixbackend.entity.Comentario;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.exception.UnauthorizedActionException;
import org.example.urbanfixbackend.mapper.ComentarioMapper;
import org.example.urbanfixbackend.repository.ComentarioRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.service.ComentarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public ComentarioResponseDTO crearComentario(Long reporteId, ComentarioCreateDTO dto, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ReporteNotFoundException(reporteId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ReporteNotFoundException("Usuario no encontrado"));

        Comentario comentario = new Comentario();
        comentario.setReporte(reporte);
        comentario.setUsuario(usuario);
        comentario.setContenido(dto.contenido());

        Comentario savedComentario = comentarioRepository.save(comentario);
        return ComentarioMapper.toDTO(savedComentario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> getComentariosByReporteId(Long reporteId) {
        List<Comentario> comentarios = comentarioRepository.findByReporteIdOrderByFechaCreacionDesc(reporteId);
        return comentarios.stream()
                .map(ComentarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ReporteNotFoundException("Comentario no encontrado"));

        if (!comentario.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedActionException("Solo el creador del comentario puede eliminarlo");
        }

        comentarioRepository.delete(comentario);
    }
}
