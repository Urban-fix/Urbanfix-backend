package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.response.UsuarioResponseDTO;
import org.example.urbanfixbackend.entity.Confirmacion;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.exception.ConfirmacionDuplicadaException;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.mapper.UsuarioMapper;
import org.example.urbanfixbackend.repository.ConfirmacionRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.service.ConfirmacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmacionServiceImpl implements ConfirmacionService {

    private final ConfirmacionRepository confirmacionRepository;
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void confirmarReporte(Long reporteId, Long usuarioId) {
        if (confirmacionRepository.existsByReporteIdAndUsuarioId(reporteId, usuarioId)) {
            throw new ConfirmacionDuplicadaException("El usuario ya ha confirmado este reporte");
        }

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ReporteNotFoundException(reporteId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ReporteNotFoundException("Usuario no encontrado"));

        Confirmacion confirmacion = new Confirmacion();
        confirmacion.setReporte(reporte);
        confirmacion.setUsuario(usuario);

        confirmacionRepository.save(confirmacion);
    }

    @Override
    public void eliminarConfirmacion(Long reporteId, Long usuarioId) {
        Confirmacion confirmacion = confirmacionRepository.findByReporteIdAndUsuarioId(reporteId, usuarioId)
                .orElseThrow(() -> new ConfirmacionDuplicadaException("El usuario no ha confirmado este reporte"));

        confirmacionRepository.delete(confirmacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getUsuariosQueConfirmaron(Long reporteId) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ReporteNotFoundException(reporteId));

        return reporte.getConfirmacionList().stream()
                .map(confirmacion -> UsuarioMapper.toDTO(confirmacion.getUsuario()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getConteoConfirmaciones(Long reporteId) {
        return confirmacionRepository.countByReporteId(reporteId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usuarioHaConfirmado(Long reporteId, Long usuarioId) {
        return confirmacionRepository.existsByReporteIdAndUsuarioId(reporteId, usuarioId);
    }
}
