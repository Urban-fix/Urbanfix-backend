package org.example.urbanfixbackend.mapper;

import org.example.urbanfixbackend.dto.request.ZonaCreateDTO;
import org.example.urbanfixbackend.dto.response.ZonaResponseDTO;
import org.example.urbanfixbackend.entity.Zona;

public class ZonaMapper {

    public static ZonaResponseDTO toDTO(Zona zona) {
        if (zona == null) {
            return null;
        }
        return new ZonaResponseDTO(
                zona.getId(),
                zona.getDistrito(),
                zona.getCoordenadasReferencia()
        );
    }

    public static Zona toEntity(ZonaCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        Zona zona = new Zona();
        zona.setDistrito(dto.distrito());
        zona.setCoordenadasReferencia(dto.coordenadasReferencia());
        return zona;
    }
}
