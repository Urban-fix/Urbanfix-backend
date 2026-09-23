package org.example.urbanfixbackend.mapper;

import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.request.ReporteUpdateDTO;
import org.example.urbanfixbackend.dto.response.ReporteDetailDTO;
import org.example.urbanfixbackend.dto.response.ReporteResponseDTO;
import org.example.urbanfixbackend.entity.Reporte;

import java.util.List;
import java.util.stream.Collectors;

public class ReporteMapper {

    public static ReporteResponseDTO toDTO(Reporte reporte, long confirmacionesCount) {
        if (reporte == null) {
            return null;
        }
        return new ReporteResponseDTO(
                reporte.getId(),
                reporte.getTitulo(),
                reporte.getDescripcion(),
                reporte.getLatitud(),
                reporte.getLongitud(),
                reporte.getFotoUrl(),
                reporte.getFechaCreacion(),
                reporte.getEstadoActual(),
                UsuarioMapper.toDTO(reporte.getUsuario()),
                CategoriaMapper.toDTO(reporte.getCategoria()),
                ZonaMapper.toDTO(reporte.getZona()),
                confirmacionesCount
        );
    }

    public static ReporteDetailDTO toDetailDTO(Reporte reporte, long confirmacionesCount) {
        if (reporte == null) {
            return null;
        }
        return new ReporteDetailDTO(
                reporte.getId(),
                reporte.getTitulo(),
                reporte.getDescripcion(),
                reporte.getLatitud(),
                reporte.getLongitud(),
                reporte.getFotoUrl(),
                reporte.getFechaCreacion(),
                reporte.getEstadoActual(),
                UsuarioMapper.toDTO(reporte.getUsuario()),
                CategoriaMapper.toDTO(reporte.getCategoria()),
                ZonaMapper.toDTO(reporte.getZona()),
                reporte.getEstadoHistorialList().stream()
                        .map(EstadoHistorialMapper::toDTO)
                        .collect(Collectors.toList()),
                reporte.getComentarioList().stream()
                        .map(ComentarioMapper::toDTO)
                        .collect(Collectors.toList()),
                reporte.getConfirmacionList().stream()
                        .map(confirmacion -> UsuarioMapper.toDTO(confirmacion.getUsuario()))
                        .collect(Collectors.toList()),
                confirmacionesCount
        );
    }

    public static Reporte toEntity(ReporteCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        Reporte reporte = new Reporte();
        reporte.setTitulo(dto.titulo());
        reporte.setDescripcion(dto.descripcion());
        reporte.setLatitud(dto.latitud());
        reporte.setLongitud(dto.longitud());
        reporte.setFotoUrl(dto.fotoUrl());
        return reporte;
    }

    public static void updateEntityFromDTO(Reporte reporte, ReporteUpdateDTO dto) {
        if (reporte == null || dto == null) {
            return;
        }
        if (dto.titulo() != null) {
            reporte.setTitulo(dto.titulo());
        }
        if (dto.descripcion() != null) {
            reporte.setDescripcion(dto.descripcion());
        }
        if (dto.latitud() != null) {
            reporte.setLatitud(dto.latitud());
        }
        if (dto.longitud() != null) {
            reporte.setLongitud(dto.longitud());
        }
        if (dto.fotoUrl() != null) {
            reporte.setFotoUrl(dto.fotoUrl());
        }
    }
}
