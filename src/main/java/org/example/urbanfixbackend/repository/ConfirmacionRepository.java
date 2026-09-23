package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.Confirmacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmacionRepository extends JpaRepository<Confirmacion, Long> {

    Optional<Confirmacion> findByReporteIdAndUsuarioId(Long reporteId, Long usuarioId);

    boolean existsByReporteIdAndUsuarioId(Long reporteId, Long usuarioId);

    long countByReporteId(Long reporteId);
}
