package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.ZonaCreateDTO;
import org.example.urbanfixbackend.dto.response.ZonaResponseDTO;
import org.example.urbanfixbackend.entity.Zona;
import org.example.urbanfixbackend.exception.ZonaNotFoundException;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.example.urbanfixbackend.service.impl.ZonaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaServiceTest {

    @Mock
    private ZonaRepository zonaRepository;

    @InjectMocks
    private ZonaServiceImpl zonaService;

    private Zona zona;

    @BeforeEach
    void setUp() {
        zona = new Zona();
        zona.setId(1L);
        zona.setNombre("Miraflores");
        zona.setCoordenadasReferencia("-12.119,-77.03");
    }

    @Test
    void createZona_Success() {
        ZonaCreateDTO dto = new ZonaCreateDTO("Miraflores", "-12.119,-77.03");
        
        when(zonaRepository.existsByNombre("Miraflores")).thenReturn(false);
        when(zonaRepository.save(any(Zona.class))).thenReturn(zona);

        ZonaResponseDTO result = zonaService.createZona(dto);

        assertNotNull(result);
        assertEquals("Miraflores", result.nombre());
        assertEquals("-12.119,-77.03", result.coordenadasReferencia());
        verify(zonaRepository).existsByNombre("Miraflores");
        verify(zonaRepository).save(any(Zona.class));
    }

    @Test
    void createZona_DuplicateName_ThrowsException() {
        ZonaCreateDTO dto = new ZonaCreateDTO("Miraflores", "-12.119,-77.03");
        
        when(zonaRepository.existsByNombre("Miraflores")).thenReturn(true);

        assertThrows(ZonaNotFoundException.class, () -> zonaService.createZona(dto));
        verify(zonaRepository).existsByNombre("Miraflores");
        verify(zonaRepository, never()).save(any(Zona.class));
    }

    @Test
    void getZonaById_Success() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));

        ZonaResponseDTO result = zonaService.getZonaById(1L);

        assertNotNull(result);
        assertEquals("Miraflores", result.nombre());
        verify(zonaRepository).findById(1L);
    }

    @Test
    void getZonaById_NotFound_ThrowsException() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> zonaService.getZonaById(1L));
        verify(zonaRepository).findById(1L);
    }

    @Test
    void getAllZonas_Success() {
        List<Zona> zonas = Arrays.asList(zona);
        when(zonaRepository.findAll()).thenReturn(zonas);

        List<ZonaResponseDTO> result = zonaService.getAllZonas();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Miraflores", result.get(0).nombre());
        verify(zonaRepository).findAll();
    }

    @Test
    void updateZona_Success() {
        ZonaCreateDTO dto = new ZonaCreateDTO("Miraflores", "-12.120,-77.04");
        
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(zonaRepository.save(any(Zona.class))).thenReturn(zona);

        ZonaResponseDTO result = zonaService.updateZona(1L, dto);

        assertNotNull(result);
        verify(zonaRepository).findById(1L);
        verify(zonaRepository).save(any(Zona.class));
    }

    @Test
    void updateZona_DuplicateName_ThrowsException() {
        ZonaCreateDTO dto = new ZonaCreateDTO("Otra", "-12.120,-77.04");
        
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(zonaRepository.existsByNombre("Otra")).thenReturn(true);

        assertThrows(ZonaNotFoundException.class, () -> zonaService.updateZona(1L, dto));
        verify(zonaRepository).findById(1L);
        verify(zonaRepository, never()).save(any(Zona.class));
    }

    @Test
    void deleteZona_Success() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));
        doNothing().when(zonaRepository).delete(any(Zona.class));

        zonaService.deleteZona(1L);

        verify(zonaRepository).findById(1L);
        verify(zonaRepository).delete(zona);
    }

    @Test
    void deleteZona_NotFound_ThrowsException() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> zonaService.deleteZona(1L));
        verify(zonaRepository).findById(1L);
        verify(zonaRepository, never()).delete(any(Zona.class));
    }
}
