package org.example.urbanfixbackend.service.impl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.PasswordResetConfirmDTO;
import org.example.urbanfixbackend.dto.request.PasswordResetRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.response.AuthResponseDTO;
import org.example.urbanfixbackend.dto.response.UsuarioResponseDTO;
import org.example.urbanfixbackend.entity.PasswordResetToken;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.exception.EmailAlreadyExistsException;
import org.example.urbanfixbackend.exception.InvalidCredentialsException;
import org.example.urbanfixbackend.exception.InvalidTokenException;
import org.example.urbanfixbackend.exception.TokenExpiredException;
import org.example.urbanfixbackend.exception.UsuarioNotFoundException;
import org.example.urbanfixbackend.mapper.UsuarioMapper;
import org.example.urbanfixbackend.repository.PasswordResetTokenRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.security.CustomUserDetails;
import org.example.urbanfixbackend.security.jwt.JwtService;
import org.example.urbanfixbackend.event.UsuarioRegistradoEvent;
import org.example.urbanfixbackend.service.AuthService;
import org.example.urbanfixbackend.service.EmailService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Usuario usuario = UsuarioMapper.toEntity(request, Rol.CIUDADANO);
        usuario.setPassword(passwordEncoder.encode(request.password()));

        Usuario savedUsuario = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioRegistradoEvent(savedUsuario));

        CustomUserDetails userDetails = new CustomUserDetails(savedUsuario);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                UsuarioMapper.toDTO(savedUsuario)
        );
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                UsuarioMapper.toDTO(userDetails.getUsuario())
        );
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        try {
            // Verifica primero que sea REALMENTE un refresh token, no un access token reutilizado
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new InvalidTokenException("El token proporcionado no es un refresh token valido");
            }

            String email = jwtService.extractUsername(refreshToken);

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(InvalidCredentialsException::new);

            CustomUserDetails userDetails = new CustomUserDetails(usuario);

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new InvalidTokenException("Refresh token invalido");
            }

            String newAccessToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            return new AuthResponseDTO(
                    newAccessToken,
                    newRefreshToken,
                    "Bearer",
                    UsuarioMapper.toDTO(usuario)
            );
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsuarioNotFoundException("No existe un usuario con el email: " + request.email()));

        // Eliminar tokens existentes para este usuario
        passwordResetTokenRepository.deleteByUsuario(usuario);

        // Crear nuevo token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setExpiryDate(expiryDate);

        passwordResetTokenRepository.save(resetToken);

        // Enviar email con el enlace de reset
        String resetLink = "https://urbanfix.com/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getNombre(), resetLink);
    }

    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidTokenException("Token de reset inválido"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new TokenExpiredException("El token de reset ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(request.newPassword()));
        usuarioRepository.save(usuario);

        // Eliminar el token usado
        passwordResetTokenRepository.delete(resetToken);
    }
}
