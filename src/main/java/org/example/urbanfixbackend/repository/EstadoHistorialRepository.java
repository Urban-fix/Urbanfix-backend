package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.EstadoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadoHistorialRepository extends JpaRepository<EstadoHistorial, Long> {

    List<EstadoHistorial> findByReporteIdOrderByFechaCambioDesc(Long reporteId);

    List<EstadoHistorial> findByUsuarioId(Long usuarioId);
}
