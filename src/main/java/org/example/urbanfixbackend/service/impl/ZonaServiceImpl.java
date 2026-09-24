package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.ZonaCreateDTO;
import org.example.urbanfixbackend.dto.response.ZonaResponseDTO;
import org.example.urbanfixbackend.entity.Zona;
import org.example.urbanfixbackend.exception.ZonaNotFoundException;
import org.example.urbanfixbackend.mapper.ZonaMapper;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.example.urbanfixbackend.service.ZonaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;

    @Override
    public ZonaResponseDTO createZona(ZonaCreateDTO dto) {
        if (zonaRepository.existsByDistrito(dto.distrito())) {
            throw new ZonaNotFoundException("Ya existe una zona con el distrito: " + dto.distrito());
        }

        Zona zona = new Zona();
        zona.setDistrito(dto.distrito());
        zona.setCoordenadasReferencia(dto.coordenadasReferencia());

        Zona savedZona = zonaRepository.save(zona);
        return ZonaMapper.toDTO(savedZona);
    }

    @Override
    @Transactional(readOnly = true)
    public ZonaResponseDTO getZonaById(Long id) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new ZonaNotFoundException(id));
        return ZonaMapper.toDTO(zona);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponseDTO> getAllZonas() {
        List<Zona> zonas = zonaRepository.findAll();
        return zonas.stream()
                .map(ZonaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ZonaResponseDTO updateZona(Long id, ZonaCreateDTO dto) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new ZonaNotFoundException(id));

        if (!zona.getDistrito().equals(dto.distrito()) && 
            zonaRepository.existsByDistrito(dto.distrito())) {
            throw new ZonaNotFoundException("Ya existe una zona con el distrito: " + dto.distrito());
        }

        zona.setDistrito(dto.distrito());
        zona.setCoordenadasReferencia(dto.coordenadasReferencia());

        Zona updatedZona = zonaRepository.save(zona);
        return ZonaMapper.toDTO(updatedZona);
    }

    @Override
    public void deleteZona(Long id) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new ZonaNotFoundException(id));
        zonaRepository.delete(zona);
    }
}
