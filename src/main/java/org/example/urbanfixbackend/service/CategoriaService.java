package org.example.urbanfixbackend.service;

import org.example.urbanfixbackend.dto.request.CategoriaCreateDTO;
import org.example.urbanfixbackend.dto.response.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    CategoriaResponseDTO createCategoria(CategoriaCreateDTO dto);

    CategoriaResponseDTO getCategoriaById(Long id);

    List<CategoriaResponseDTO> getAllCategorias();

    CategoriaResponseDTO updateCategoria(Long id, CategoriaCreateDTO dto);

    void deleteCategoria(Long id);
}
