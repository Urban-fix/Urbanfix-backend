package org.example.urbanfixbackend.mapper;

import org.example.urbanfixbackend.dto.request.CategoriaCreateDTO;
import org.example.urbanfixbackend.dto.response.CategoriaResponseDTO;
import org.example.urbanfixbackend.entity.Categoria;

public class CategoriaMapper {

    public static CategoriaResponseDTO toDTO(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }

    public static Categoria toEntity(CategoriaCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());
        return categoria;
    }
}
