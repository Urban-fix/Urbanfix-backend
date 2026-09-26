package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.request.ReporteUpdateDTO;
import org.example.urbanfixbackend.dto.response.ReporteDetailDTO;
import org.example.urbanfixbackend.dto.response.ReporteResponseDTO;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;

import java.util.List;

public interface ReporteService {

    ReporteResponseDTO createReporte(ReporteCreateDTO dto, Long usuarioId);

    ReporteResponseDTO getReporteById(Long id);

    ReporteDetailDTO getReporteDetailById(Long id);

    List<ReporteResponseDTO> getAllReportes();

    List<ReporteResponseDTO> getReportesByEstado(EstadoReporte estado);

    List<ReporteResponseDTO> getReportesByCategoria(Long categoriaId);

    List<ReporteResponseDTO> getReportesByZona(Long zonaId);

    List<ReporteResponseDTO> getReportesByUsuario(Long usuarioId);

    List<ReporteResponseDTO> searchReportes(String query);

    ReporteResponseDTO updateReporte(Long id, ReporteUpdateDTO dto, Long usuarioId);

    void deleteReporte(Long id, Long usuarioId);

    void cambiarEstado(Long id, EstadoReporte nuevoEstado, Long usuarioId);
}
