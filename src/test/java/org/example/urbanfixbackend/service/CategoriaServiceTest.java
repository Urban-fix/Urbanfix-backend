package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.CategoriaCreateDTO;
import org.example.urbanfixbackend.dto.response.CategoriaResponseDTO;
import org.example.urbanfixbackend.entity.Categoria;
import org.example.urbanfixbackend.exception.CategoriaNotFoundException;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.service.impl.CategoriaServiceImpl;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Vialidad");
        categoria.setDescripcion("Problemas viales");
    }

    @Test
    void createCategoria_Success() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Problemas viales");
        
        when(categoriaRepository.existsByNombre("Vialidad")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaResponseDTO result = categoriaService.createCategoria(dto);

        assertNotNull(result);
        assertEquals("Vialidad", result.nombre());
        assertEquals("Problemas viales", result.descripcion());
        verify(categoriaRepository).existsByNombre("Vialidad");
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void createCategoria_DuplicateName_ThrowsException() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Problemas viales");
        
        when(categoriaRepository.existsByNombre("Vialidad")).thenReturn(true);

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.createCategoria(dto));
        verify(categoriaRepository).existsByNombre("Vialidad");
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void getCategoriaById_Success() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaResponseDTO result = categoriaService.getCategoriaById(1L);

        assertNotNull(result);
        assertEquals("Vialidad", result.nombre());
        verify(categoriaRepository).findById(1L);
    }

    @Test
    void getCategoriaById_NotFound_ThrowsException() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.getCategoriaById(1L));
        verify(categoriaRepository).findById(1L);
    }

    @Test
    void getAllCategorias_Success() {
        List<Categoria> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        List<CategoriaResponseDTO> result = categoriaService.getAllCategorias();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Vialidad", result.get(0).nombre());
        verify(categoriaRepository).findAll();
    }

    @Test
    void updateCategoria_Success() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Nueva descripción");
        
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaResponseDTO result = categoriaService.updateCategoria(1L, dto);

        assertNotNull(result);
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void updateCategoria_DuplicateName_ThrowsException() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Otra", "Descripción");
        
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombre("Otra")).thenReturn(true);

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.updateCategoria(1L, dto));
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void deleteCategoria_Success() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        doNothing().when(categoriaRepository).delete(any(Categoria.class));

        categoriaService.deleteCategoria(1L);

        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void deleteCategoria_NotFound_ThrowsException() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> categoriaService.deleteCategoria(1L));
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }
}
