package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.Reporte;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByEstadoActual(EstadoReporte estadoActual);

    List<Reporte> findByUsuarioId(Long usuarioId);

    List<Reporte> findByCategoriaId(Long categoriaId);

    List<Reporte> findByZonaId(Long zonaId);

    @Query("SELECT r FROM Reporte r WHERE r.estadoActual = :estado ORDER BY r.fechaCreacion DESC")
    List<Reporte> findByEstadoActualOrderByFechaCreacionDesc(@Param("estado") EstadoReporte estado);

    @Query("SELECT r FROM Reporte r WHERE r.titulo LIKE %:titulo% OR r.descripcion LIKE %:descripcion%")
    List<Reporte> searchByTituloOrDescripcion(@Param("titulo") String titulo, @Param("descripcion") String descripcion);

    @Query("SELECT r FROM Reporte r WHERE r.latitud BETWEEN :latMin AND :latMax AND r.longitud BETWEEN :lonMin AND :lonMax")
    List<Reporte> findByCoordenadasRango(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("lonMin") Double lonMin,
            @Param("lonMax") Double lonMax
    );

    @Query("SELECT r FROM Reporte r WHERE r.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Reporte> findByFechaCreacionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT r FROM Reporte r WHERE r.zona.id = :zonaId AND r.estadoActual = :estado")
    List<Reporte> findByZonaIdAndEstadoActual(
            @Param("zonaId") Long zonaId,
            @Param("estado") EstadoReporte estado
    );

    @Query("SELECT COUNT(r) FROM Reporte r WHERE r.estadoActual = :estado")
    long countByEstadoActual(@Param("estado") EstadoReporte estado);
}
