package org.example.urbanfixbackend.mapper;

import org.example.urbanfixbackend.dto.response.EstadoHistorialResponseDTO;
import org.example.urbanfixbackend.entity.EstadoHistorial;

public class EstadoHistorialMapper {

    public static EstadoHistorialResponseDTO toDTO(EstadoHistorial historial) {
        if (historial == null) {
            return null;
        }
        return new EstadoHistorialResponseDTO(
                historial.getId(),
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getFechaCambio(),
                UsuarioMapper.toDTO(historial.getUsuario())
        );
    }
}
