package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.ZonaCreateDTO;
import org.example.urbanfixbackend.dto.response.ZonaResponseDTO;

import java.util.List;

public interface ZonaService {

    ZonaResponseDTO createZona(ZonaCreateDTO dto);

    ZonaResponseDTO getZonaById(Long id);

    List<ZonaResponseDTO> getAllZonas();

    ZonaResponseDTO updateZona(Long id, ZonaCreateDTO dto);

    void deleteZona(Long id);
}
