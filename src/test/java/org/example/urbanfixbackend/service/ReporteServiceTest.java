package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.request.ReporteUpdateDTO;
import org.example.urbanfixbackend.dto.response.ReporteResponseDTO;
import org.example.urbanfixbackend.entity.Categoria;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.Zona;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.exception.UnauthorizedActionException;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.repository.ConfirmacionRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.example.urbanfixbackend.service.impl.ReporteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private CategoriaRepository categoriaRepository;
    
    @Mock
    private ZonaRepository zonaRepository;
    
    @Mock
    private ConfirmacionRepository confirmacionRepository;
    
    @Mock
    private EstadoHistorialService estadoHistorialService;
    
    @InjectMocks
    private ReporteServiceImpl reporteService;
    
    private Usuario usuario;
    private Categoria categoria;
    private Zona zona;
    private Reporte reporte;
    private ReporteCreateDTO createDTO;
    
    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Vialidad");
        
        zona = new Zona();
        zona.setId(1L);
        zona.setDistrito("Miraflores");
        
        reporte = new Reporte();
        reporte.setId(1L);
        reporte.setTitulo("Bache en la calle");
        reporte.setEstadoActual(EstadoReporte.REPORTADO);
        reporte.setUsuario(usuario);
        reporte.setCategoria(categoria);
        reporte.setZona(zona);
        
        createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                1L,
                1L
        );
    }
    
    @Test
    void createReporte_Success() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporte);
        when(confirmacionRepository.countByReporteId(1L)).thenReturn(0L);
        
        ReporteResponseDTO result = reporteService.createReporte(createDTO, 1L);
        
        assertNotNull(result);
        assertEquals("Bache en la calle", result.titulo());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }
    
    @Test
    void getReporteById_Success() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(confirmacionRepository.countByReporteId(1L)).thenReturn(5L);
        
        ReporteResponseDTO result = reporteService.getReporteById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(5L, result.confirmacionesCount());
    }
    
    @Test
    void getReporteById_NotFound() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ReporteNotFoundException.class, () -> reporteService.getReporteById(1L));
    }
    
    @Test
    void updateReporte_Success() {
        ReporteUpdateDTO updateDTO = new ReporteUpdateDTO("Bache reparado", "El bache fue arreglado", -12.119, -77.03, "foto2.jpg");
        
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporte);
        when(confirmacionRepository.countByReporteId(1L)).thenReturn(5L);
        
        ReporteResponseDTO result = reporteService.updateReporte(1L, updateDTO, 1L);
        
        assertNotNull(result);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }
    
    @Test
    void updateReporte_Unauthorized() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        reporte.setUsuario(otroUsuario);
        
        ReporteUpdateDTO updateDTO = new ReporteUpdateDTO("Bache reparado", "El bache fue arreglado", -12.119, -77.03, "foto2.jpg");
        
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        
        assertThrows(UnauthorizedActionException.class, () -> 
                reporteService.updateReporte(1L, updateDTO, 1L));
    }
    
    @Test
    void deleteReporte_Success() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        
        reporteService.deleteReporte(1L, 1L);
        
        verify(reporteRepository, times(1)).delete(reporte);
    }
    
    @Test
    void deleteReporte_Unauthorized() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        reporte.setUsuario(otroUsuario);
        
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        
        assertThrows(UnauthorizedActionException.class, () -> reporteService.deleteReporte(1L, 1L));
    }
}
