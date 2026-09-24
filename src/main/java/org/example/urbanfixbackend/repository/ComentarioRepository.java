package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByReporteId(Long reporteId);

    List<Comentario> findByUsuarioId(Long usuarioId);

    List<Comentario> findByReporteIdOrderByFechaCreacionAsc(Long reporteId);
}