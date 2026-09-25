package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.CambioEstadoDTO;
import org.example.urbanfixbackend.dto.response.EstadoHistorialResponseDTO;
import org.example.urbanfixbackend.entity.EstadoHistorial;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.exception.EstadoTransicionInvalidaException;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.exception.UnauthorizedActionException;
import org.example.urbanfixbackend.repository.EstadoHistorialRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.service.impl.EstadoHistorialServiceImpl;
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
class EstadoHistorialServiceTest {

    @Mock
    private EstadoHistorialRepository estadoHistorialRepository;

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EstadoHistorialServiceImpl estadoHistorialService;

    private Reporte reporte;
    private Usuario usuario;
    private EstadoHistorial estadoHistorial;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("admin@example.com");
        usuario.setRol(Rol.ADMIN_MUNICIPAL);

        reporte = new Reporte();
        reporte.setId(1L);
        reporte.setTitulo("Bache en calle");
        reporte.setEstadoActual(EstadoReporte.REPORTADO);
        reporte.setUsuario(usuario);

        estadoHistorial = new EstadoHistorial();
        estadoHistorial.setId(1L);
        estadoHistorial.setReporte(reporte);
        estadoHistorial.setUsuario(usuario);
        estadoHistorial.setEstadoAnterior(EstadoReporte.REPORTADO);
        estadoHistorial.setEstadoNuevo(EstadoReporte.EN_PROCESO);
    }

    @Test
    void cambiarEstado_Success() {
        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoReporte.EN_PROCESO);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(estadoHistorialRepository.save(any(EstadoHistorial.class))).thenReturn(estadoHistorial);

        EstadoHistorialResponseDTO result = estadoHistorialService.cambiarEstado(1L, dto, 1L);

        assertNotNull(result);
        assertEquals(EstadoReporte.EN_PROCESO, result.estadoNuevo());
        verify(estadoHistorialRepository, times(1)).save(any(EstadoHistorial.class));
        verify(reporteRepository, times(1)).save(reporte);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void cambiarEstado_ReporteNotFound() {
        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoReporte.EN_PROCESO);

        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> estadoHistorialService.cambiarEstado(1L, dto, 1L));
    }

    @Test
    void cambiarEstado_UsuarioNotFound() {
        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoReporte.EN_PROCESO);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> estadoHistorialService.cambiarEstado(1L, dto, 1L));
    }

    @Test
    void cambiarEstado_Unauthorized() {
        usuario.setRol(Rol.CIUDADANO);
        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoReporte.EN_PROCESO);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(UnauthorizedActionException.class, () -> estadoHistorialService.cambiarEstado(1L, dto, 1L));
    }

    @Test
    void cambiarEstado_SameState() {
        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoReporte.REPORTADO);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(EstadoTransicionInvalidaException.class, () -> estadoHistorialService.cambiarEstado(1L, dto, 1L));
    }

    @Test
    void cambiarEstado_InvalidTransition() {
        CambioEstadoDTO dto = new CambioEstadoDTO(EstadoReporte.RESUELTO);

        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(EstadoTransicionInvalidaException.class, () -> estadoHistorialService.cambiarEstado(1L, dto, 1L));
    }

    @Test
    void getHistorialByReporteId_Success() {
        when(estadoHistorialRepository.findByReporteIdOrderByFechaCambioDesc(1L))
                .thenReturn(Arrays.asList(estadoHistorial));

        List<EstadoHistorialResponseDTO> result = estadoHistorialService.getHistorialByReporteId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void validarTransicionEstado_ReportadoToEnProceso_Success() {
        assertDoesNotThrow(() -> 
                estadoHistorialService.validarTransicionEstado(EstadoReporte.REPORTADO, EstadoReporte.EN_PROCESO));
    }

    @Test
    void validarTransicionEstado_ReportadoToRechazado_Success() {
        assertDoesNotThrow(() -> 
                estadoHistorialService.validarTransicionEstado(EstadoReporte.REPORTADO, EstadoReporte.RECHAZADO));
    }

    @Test
    void validarTransicionEstado_EnProcesoToResuelto_Success() {
        assertDoesNotThrow(() -> 
                estadoHistorialService.validarTransicionEstado(EstadoReporte.EN_PROCESO, EstadoReporte.RESUELTO));
    }

    @Test
    void validarTransicionEstado_EnProcesoToRechazado_Success() {
        assertDoesNotThrow(() -> 
                estadoHistorialService.validarTransicionEstado(EstadoReporte.EN_PROCESO, EstadoReporte.RECHAZADO));
    }

    @Test
    void validarTransicionEstado_ResueltoToAny_Fails() {
        assertThrows(EstadoTransicionInvalidaException.class, () -> 
                estadoHistorialService.validarTransicionEstado(EstadoReporte.RESUELTO, EstadoReporte.REPORTADO));
    }

    @Test
    void validarTransicionEstado_RechazadoToAny_Fails() {
        assertThrows(EstadoTransicionInvalidaException.class, () -> 
                estadoHistorialService.validarTransicionEstado(EstadoReporte.RECHAZADO, EstadoReporte.REPORTADO));
    }

    @Test
    void validarTransicionEstado_SameState_Fails() {
        assertThrows(EstadoTransicionInvalidaException.class, () -> 
                estadoHistorialService.validarTransicionEstado(EstadoReporte.REPORTADO, EstadoReporte.REPORTADO));
    }
}
