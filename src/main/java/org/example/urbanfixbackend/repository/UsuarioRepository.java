package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol")
    List<Usuario> findByRol(@Param("rol") Rol rol);

    @Query("SELECT u FROM Usuario u WHERE u.nombre LIKE %:nombre% OR u.apellido LIKE %:apellido%")
    List<Usuario> searchByNombreOrApellido(@Param("nombre") String nombre, @Param("apellido") String apellido);
}
