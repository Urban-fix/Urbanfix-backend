package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    @Query("SELECT c FROM Categoria c WHERE c.nombre LIKE %:nombre%")
    List<Categoria> searchByNombre(@Param("nombre") String nombre);
}
