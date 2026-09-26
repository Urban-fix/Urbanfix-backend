package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonaRepository extends JpaRepository<Zona, Long> {

    Optional<Zona> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    @Query("SELECT z FROM Zona z WHERE z.nombre LIKE %:nombre%")
    List<Zona> searchByNombre(@Param("nombre") String nombre);
}
