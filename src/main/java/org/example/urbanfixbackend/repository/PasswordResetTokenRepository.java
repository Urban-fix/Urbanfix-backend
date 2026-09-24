package org.example.urbanfixbackend.repository;

import org.example.urbanfixbackend.entity.PasswordResetToken;
import org.example.urbanfixbackend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUsuario(Usuario usuario);

    void deleteByUsuario(Usuario usuario);

    void deleteByToken(String token);
}
