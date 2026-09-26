package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface ConfirmacionService {

    void confirmarReporte(Long reporteId, Long usuarioId);

    void eliminarConfirmacion(Long reporteId, Long usuarioId);

    List<UsuarioResponseDTO> getUsuariosQueConfirmaron(Long reporteId);

    long getConteoConfirmaciones(Long reporteId);

    boolean usuarioHaConfirmado(Long reporteId, Long usuarioId);
}
