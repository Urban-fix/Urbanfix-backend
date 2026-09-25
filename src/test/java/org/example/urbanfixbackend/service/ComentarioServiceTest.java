package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.ComentarioCreateDTO;
import org.example.urbanfixbackend.dto.response.ComentarioResponseDTO;
import org.example.urbanfixbackend.entity.Comentario;
import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.exception.ReporteNotFoundException;
import org.example.urbanfixbackend.exception.UnauthorizedActionException;
import org.example.urbanfixbackend.repository.ComentarioRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.service.impl.ComentarioServiceImpl;
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
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ComentarioServiceImpl comentarioService;

    private Reporte reporte;
    private Usuario usuario;
    private Comentario comentario;

    @BeforeEach
    void setUp() {
        reporte = new Reporte();
        reporte.setId(1L);
        reporte.setTitulo("Bache en la calle");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test");
        usuario.setEmail("test@example.com");

        comentario = new Comentario();
        comentario.setId(1L);
        comentario.setReporte(reporte);
        comentario.setUsuario(usuario);
        comentario.setContenido("Este es un comentario");
    }

    @Test
    void crearComentario_Success() {
        ComentarioCreateDTO dto = new ComentarioCreateDTO("Este es un comentario");
        
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

        ComentarioResponseDTO result = comentarioService.crearComentario(1L, dto, 1L);

        assertNotNull(result);
        assertEquals("Este es un comentario", result.contenido());
        verify(reporteRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(comentarioRepository).save(any(Comentario.class));
    }

    @Test
    void crearComentario_ReporteNotFound_ThrowsException() {
        ComentarioCreateDTO dto = new ComentarioCreateDTO("Este es un comentario");
        
        when(reporteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> comentarioService.crearComentario(1L, dto, 1L));
        verify(reporteRepository).findById(1L);
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void crearComentario_UsuarioNotFound_ThrowsException() {
        ComentarioCreateDTO dto = new ComentarioCreateDTO("Este es un comentario");
        
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> comentarioService.crearComentario(1L, dto, 1L));
        verify(reporteRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void getComentariosByReporteId_Success() {
        List<Comentario> comentarios = Arrays.asList(comentario);
        when(comentarioRepository.findByReporteIdOrderByFechaCreacionAsc(1L)).thenReturn(comentarios);

        List<ComentarioResponseDTO> result = comentarioService.getComentariosByReporteId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Este es un comentario", result.get(0).contenido());
        verify(comentarioRepository).findByReporteIdOrderByFechaCreacionAsc(1L);
    }

    @Test
    void eliminarComentario_Success() {
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentario));
        doNothing().when(comentarioRepository).delete(any(Comentario.class));

        comentarioService.eliminarComentario(1L, 1L);

        verify(comentarioRepository).findById(1L);
        verify(comentarioRepository).delete(comentario);
    }

    @Test
    void eliminarComentario_NotFound_ThrowsException() {
        when(comentarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> comentarioService.eliminarComentario(1L, 1L));
        verify(comentarioRepository).findById(1L);
        verify(comentarioRepository, never()).delete(any(Comentario.class));
    }

    @Test
    void eliminarComentario_Unauthorized_ThrowsException() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        comentario.setUsuario(otroUsuario);
        
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentario));

        assertThrows(UnauthorizedActionException.class, () -> comentarioService.eliminarComentario(1L, 1L));
        verify(comentarioRepository).findById(1L);
        verify(comentarioRepository, never()).delete(any(Comentario.class));
    }
}
