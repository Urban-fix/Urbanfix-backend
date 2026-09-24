package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.CambioEstadoDTO;
import org.example.urbanfixbackend.dto.response.EstadoHistorialResponseDTO;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;

import java.util.List;

public interface EstadoHistorialService {

    EstadoHistorialResponseDTO cambiarEstado(Long reporteId, CambioEstadoDTO dto, Long usuarioId);

    List<EstadoHistorialResponseDTO> getHistorialByReporteId(Long reporteId);

    void validarTransicionEstado(EstadoReporte estadoActual, EstadoReporte estadoNuevo);
}
