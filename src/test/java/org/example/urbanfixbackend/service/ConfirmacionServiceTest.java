package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.response.UsuarioResponseDTO;
import org.example.urbanfixbackend.entity.Confirmacion;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.exception.ConfirmacionDuplicadaException;
import org.example.urbanfixbackend.exception.ConfirmacionNotFoundException;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.mapper.UsuarioMapper;
import org.example.urbanfixbackend.repository.ConfirmacionRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.service.impl.ConfirmacionServiceImpl;
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
class ConfirmacionServiceTest {

    @Mock
    private ConfirmacionRepository confirmacionRepository;

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConfirmacionServiceImpl confirmacionService;

    private Reporte reporte;
    private Usuario usuario;
    private Confirmacion confirmacion;

    @BeforeEach
    void setUp() {
        reporte = new Reporte();
        reporte.setId(1L);
        reporte.setTitulo("Bache en la calle");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test");
        usuario.setEmail("test@example.com");

        confirmacion = new Confirmacion();
        confirmacion.setId(1L);
        confirmacion.setReporte(reporte);
        confirmacion.setUsuario(usuario);
    }

    @Test
    void confirmarReporte_Success() {
        when(confirmacionRepository.existsByReporteIdAndUsuarioId(1L, 1L)).thenReturn(false);
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(confirmacionRepository.save(any(Confirmacion.class))).thenReturn(confirmacion);

        confirmacionService.confirmarReporte(1L, 1L);

        verify(confirmacionRepository).existsByReporteIdAndUsuarioId(1L, 1L);
        verify(reporteRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(confirmacionRepository).save(any(Confirmacion.class));
    }

    @Test
    void confirmarReporte_Duplicate_ThrowsException() {
        when(confirmacionRepository.existsByReporteIdAndUsuarioId(1L, 1L)).thenReturn(true);

        assertThrows(ConfirmacionDuplicadaException.class, () -> confirmacionService.confirmarReporte(1L, 1L));
        verify(confirmacionRepository).existsByReporteIdAndUsuarioId(1L, 1L);
        verify(reporteRepository, never()).findById(anyLong());
        verify(confirmacionRepository, never()).save(any(Confirmacion.class));
    }

    @Test
    void confirmarReporte_ReporteNotFound_ThrowsException() {
        when(confirmacionRepository.existsByReporteIdAndUsuarioId(1L, 1L)).thenReturn(false);
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> confirmacionService.confirmarReporte(1L, 1L));
        verify(confirmacionRepository).existsByReporteIdAndUsuarioId(1L, 1L);
        verify(reporteRepository).findById(1L);
        verify(confirmacionRepository, never()).save(any(Confirmacion.class));
    }

    @Test
    void eliminarConfirmacion_Success() {
        when(confirmacionRepository.findByReporteIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(confirmacion));
        doNothing().when(confirmacionRepository).delete(any(Confirmacion.class));

        confirmacionService.eliminarConfirmacion(1L, 1L);

        verify(confirmacionRepository).findByReporteIdAndUsuarioId(1L, 1L);
        verify(confirmacionRepository).delete(confirmacion);
    }

    @Test
    void eliminarConfirmacion_NotFound_ThrowsException() {
        when(confirmacionRepository.findByReporteIdAndUsuarioId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ConfirmacionNotFoundException.class, () -> confirmacionService.eliminarConfirmacion(1L, 1L));
        verify(confirmacionRepository).findByReporteIdAndUsuarioId(1L, 1L);
        verify(confirmacionRepository, never()).delete(any(Confirmacion.class));
    }

    @Test
    void getUsuariosQueConfirmaron_Success() {
        reporte.setConfirmacionList(Arrays.asList(confirmacion));
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        List<UsuarioResponseDTO> result = confirmacionService.getUsuariosQueConfirmaron(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reporteRepository).findById(1L);
    }

    @Test
    void getUsuariosQueConfirmaron_ReporteNotFound_ThrowsException() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> confirmacionService.getUsuariosQueConfirmaron(1L));
        verify(reporteRepository).findById(1L);
    }

    @Test
    void getConteoConfirmaciones_Success() {
        when(confirmacionRepository.countByReporteId(1L)).thenReturn(5L);

        long result = confirmacionService.getConteoConfirmaciones(1L);

        assertEquals(5L, result);
        verify(confirmacionRepository).countByReporteId(1L);
    }

    @Test
    void usuarioHaConfirmado_True() {
        when(confirmacionRepository.existsByReporteIdAndUsuarioId(1L, 1L)).thenReturn(true);

        boolean result = confirmacionService.usuarioHaConfirmado(1L, 1L);

        assertTrue(result);
        verify(confirmacionRepository).existsByReporteIdAndUsuarioId(1L, 1L);
    }

    @Test
    void usuarioHaConfirmado_False() {
        when(confirmacionRepository.existsByReporteIdAndUsuarioId(1L, 1L)).thenReturn(false);

        boolean result = confirmacionService.usuarioHaConfirmado(1L, 1L);

        assertFalse(result);
        verify(confirmacionRepository).existsByReporteIdAndUsuarioId(1L, 1L);
    }
}
