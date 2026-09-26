package org.example.urbanfixbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.CategoriaCreateDTO;
import org.example.urbanfixbackend.dto.response.CategoriaResponseDTO;
import org.example.urbanfixbackend.entity.Categoria;
import org.example.urbanfixbackend.exception.CategoriaNotFoundException;
import org.example.urbanfixbackend.mapper.CategoriaMapper;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.service.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponseDTO createCategoria(CategoriaCreateDTO dto) {
        if (categoriaRepository.existsByNombre(dto.nombre())) {
            throw new CategoriaNotFoundException("Ya existe una categoría con el nombre: " + dto.nombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());

        Categoria savedCategoria = categoriaRepository.save(categoria);
        return CategoriaMapper.toDTO(savedCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO getCategoriaById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        return CategoriaMapper.toDTO(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getAllCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .map(CategoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaResponseDTO updateCategoria(Long id, CategoriaCreateDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        if (!categoria.getNombre().equals(dto.nombre()) && 
            categoriaRepository.existsByNombre(dto.nombre())) {
            throw new CategoriaNotFoundException("Ya existe una categoría con el nombre: " + dto.nombre());
        }

        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());

        Categoria updatedCategoria = categoriaRepository.save(categoria);
        return CategoriaMapper.toDTO(updatedCategoria);
    }

    @Override
    public void deleteCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        categoriaRepository.delete(categoria);
    }
}
