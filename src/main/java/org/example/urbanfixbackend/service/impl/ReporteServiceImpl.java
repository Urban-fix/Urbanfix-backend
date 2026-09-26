package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.request.ReporteUpdateDTO;
import org.example.urbanfixbackend.dto.response.ReporteDetailDTO;
import org.example.urbanfixbackend.dto.response.ReporteResponseDTO;
import org.example.urbanfixbackend.entity.Categoria;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.Zona;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.example.urbanfixbackend.exception.CategoriaNotFoundException;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.exception.UnauthorizedActionException;
import org.example.urbanfixbackend.exception.ZonaNotFoundException;
import org.example.urbanfixbackend.mapper.ReporteMapper;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.repository.ConfirmacionRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.example.urbanfixbackend.event.ReporteCreadoEvent;
import org.example.urbanfixbackend.service.EstadoHistorialService;
import org.example.urbanfixbackend.service.ReporteService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReporteServiceImpl implements ReporteService {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ZonaRepository zonaRepository;
    private final ConfirmacionRepository confirmacionRepository;
    private final EstadoHistorialService estadoHistorialService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ReporteResponseDTO createReporte(ReporteCreateDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ReporteNotFoundException("Usuario no encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new CategoriaNotFoundException(dto.categoriaId()));

        Zona zona = zonaRepository.findById(dto.zonaId())
                .orElseThrow(() -> new ZonaNotFoundException(dto.zonaId()));

        Reporte reporte = ReporteMapper.toEntity(dto);
        reporte.setUsuario(usuario);
        reporte.setCategoria(categoria);
        reporte.setZona(zona);

        Reporte savedReporte = reporteRepository.save(reporte);
        long confirmacionesCount = confirmacionRepository.countByReporteId(savedReporte.getId());

        return ReporteMapper.toDTO(savedReporte, confirmacionesCount);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDTO getReporteById(Long id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
        long confirmacionesCount = confirmacionRepository.countByReporteId(id);
        return ReporteMapper.toDTO(reporte, confirmacionesCount);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteDetailDTO getReporteDetailById(Long id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
        long confirmacionesCount = confirmacionRepository.countByReporteId(id);
        return ReporteMapper.toDetailDTO(reporte, confirmacionesCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> getAllReportes() {
        List<Reporte> reportes = reporteRepository.findAll();
        return reportes.stream()
                .map(reporte -> ReporteMapper.toDTO(reporte, 
                        confirmacionRepository.countByReporteId(reporte.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> getReportesByEstado(org.example.urbanfixbackend.entity.enums.EstadoReporte estado) {
        List<Reporte> reportes = reporteRepository.findByEstadoActual(estado);
        return reportes.stream()
                .map(reporte -> ReporteMapper.toDTO(reporte, 
                        confirmacionRepository.countByReporteId(reporte.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> getReportesByCategoria(Long categoriaId) {
        List<Reporte> reportes = reporteRepository.findByCategoriaId(categoriaId);
        return reportes.stream()
                .map(reporte -> ReporteMapper.toDTO(reporte, 
                        confirmacionRepository.countByReporteId(reporte.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> getReportesByZona(Long zonaId) {
        List<Reporte> reportes = reporteRepository.findByZonaId(zonaId);
        return reportes.stream()
                .map(reporte -> ReporteMapper.toDTO(reporte, 
                        confirmacionRepository.countByReporteId(reporte.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> getReportesByUsuario(Long usuarioId) {
        List<Reporte> reportes = reporteRepository.findByUsuarioId(usuarioId);
        return reportes.stream()
                .map(reporte -> ReporteMapper.toDTO(reporte, 
                        confirmacionRepository.countByReporteId(reporte.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> searchReportes(String query) {
        List<Reporte> reportes = reporteRepository.searchByTituloOrDescripcion(query, query);
        return reportes.stream()
                .map(reporte -> ReporteMapper.toDTO(reporte, 
                        confirmacionRepository.countByReporteId(reporte.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ReporteResponseDTO updateReporte(Long id, ReporteUpdateDTO dto, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));

        if (!reporte.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedActionException("Solo el creador del reporte puede editarlo");
        }

        ReporteMapper.updateEntityFromDTO(reporte, dto);
        Reporte updatedReporte = reporteRepository.save(reporte);
        long confirmacionesCount = confirmacionRepository.countByReporteId(id);

        return ReporteMapper.toDTO(updatedReporte, confirmacionesCount);
    }

    @Override
    public void deleteReporte(Long id, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));

        if (!reporte.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedActionException("Solo el creador del reporte puede eliminarlo");
        }

        reporteRepository.delete(reporte);
    }

    @Override
    public void cambiarEstado(Long id, EstadoReporte nuevoEstado, Long usuarioId) {
        estadoHistorialService.cambiarEstado(id, 
                new org.example.urbanfixbackend.dto.request.CambioEstadoDTO(nuevoEstado), 
                usuarioId);
    }
}
