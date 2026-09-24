package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.CambioEstadoDTO;
import org.example.urbanfixbackend.dto.response.EstadoHistorialResponseDTO;
import org.example.urbanfixbackend.entity.EstadoHistorial;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.example.urbanfixbackend.exception.EstadoTransicionInvalidaException;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.exception.UnauthorizedActionException;
import org.example.urbanfixbackend.mapper.EstadoHistorialMapper;
import org.example.urbanfixbackend.repository.EstadoHistorialRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.event.EstadoCambiadoEvent;
import org.example.urbanfixbackend.service.EstadoHistorialService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EstadoHistorialServiceImpl implements EstadoHistorialService {

    private final EstadoHistorialRepository estadoHistorialRepository;
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public EstadoHistorialResponseDTO cambiarEstado(Long reporteId, CambioEstadoDTO dto, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ReporteNotFoundException(reporteId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ReporteNotFoundException("Usuario no encontrado"));

        EstadoReporte estadoActual = reporte.getEstadoActual();
        EstadoReporte estadoNuevo = dto.nuevoEstado();

        validarTransicionEstado(estadoActual, estadoNuevo);

        EstadoHistorial estadoHistorial = new EstadoHistorial();
        estadoHistorial.setReporte(reporte);
        estadoHistorial.setUsuario(usuario);
        estadoHistorial.setEstadoAnterior(estadoActual);
        estadoHistorial.setEstadoNuevo(estadoNuevo);

        EstadoHistorial savedEstadoHistorial = estadoHistorialRepository.save(estadoHistorial);

        reporte.setEstadoActual(estadoNuevo);
        reporteRepository.save(reporte);

        eventPublisher.publishEvent(new EstadoCambiadoEvent(reporte, estadoActual, estadoNuevo));

        return EstadoHistorialMapper.toDTO(savedEstadoHistorial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoHistorialResponseDTO> getHistorialByReporteId(Long reporteId) {
        List<EstadoHistorial> historial = estadoHistorialRepository.findByReporteIdOrderByFechaCambioDesc(reporteId);
        return historial.stream()
                .map(EstadoHistorialMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void validarTransicionEstado(EstadoReporte estadoActual, EstadoReporte estadoNuevo) {
        if (estadoActual == estadoNuevo) {
            throw new EstadoTransicionInvalidaException(
                    "El estado nuevo es igual al estado actual: " + estadoActual
            );
        }

        boolean transicionValida = switch (estadoActual) {
            case REPORTADO -> estadoNuevo == EstadoReporte.EN_PROCESO;
            case EN_PROCESO -> estadoNuevo == EstadoReporte.RESUELTO;
            case RESUELTO -> false;
        };

        if (!transicionValida) {
            throw new EstadoTransicionInvalidaException(
                    "Transición de estado inválida: de " + estadoActual + " a " + estadoNuevo
            );
        }
    }
}
