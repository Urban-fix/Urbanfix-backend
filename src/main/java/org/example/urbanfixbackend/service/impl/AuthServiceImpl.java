package org.example.urbanfixbackend.service.impl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.CreateUserDTO;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.PasswordResetConfirmDTO;
import org.example.urbanfixbackend.dto.request.PasswordResetRequestDTO;
import org.example.urbanfixbackend.dto.request.PasswordResetValidateDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.response.AuthResponseDTO;
import org.example.urbanfixbackend.dto.response.PasswordResetResponseDTO;
import org.example.urbanfixbackend.dto.response.PasswordResetValidateResponseDTO;
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
import org.example.urbanfixbackend.security.RateLimiterService;
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
    private final RateLimiterService rateLimiterService;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Todos los registros públicos son CIUDADANO por defecto
        // Solo los admins pueden crear usuarios con otros roles vía endpoint específico
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
    public PasswordResetResponseDTO requestPasswordReset(PasswordResetRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email()).orElse(null);

        if (usuario == null) {
            // No revelar si el email existe o no (seguridad)
            return new PasswordResetResponseDTO(
                    "Si el email está registrado, recibirás un código de recuperación",
                    null
            );
        }

        // Eliminar tokens existentes para este usuario
        passwordResetTokenRepository.deleteByUsuario(usuario);

        // Generar código de 6 dígitos
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(2);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(code);
        resetToken.setUsuario(usuario);
        resetToken.setExpiryDate(expiryDate);

        passwordResetTokenRepository.save(resetToken);

        // Enviar email con el código de 6 dígitos
        emailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getNombre(), code);

        return new PasswordResetResponseDTO(
                "Email de recuperación enviado a " + usuario.getEmail(),
                null
        );
    }

    @Override
    @Transactional
    public PasswordResetValidateResponseDTO validatePasswordReset(PasswordResetValidateDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.code())
                .orElse(null);

        if (resetToken == null) {
            return new PasswordResetValidateResponseDTO(false, "Código inválido");
        }

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            return new PasswordResetValidateResponseDTO(false, "El código ha expirado. Solicita uno nuevo.");
        }

        return new PasswordResetValidateResponseDTO(true, "Código válido");
    }

    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmDTO request) {
        // Rate limiting: máximo 3 intentos por token en 2 minutos
        String rateLimitKey = "password-reset:" + request.token();
        if (!rateLimiterService.tryConsume(rateLimitKey)) {
            throw new InvalidTokenException("Demasiados intentos. Por favor, solicita un nuevo código.");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidTokenException("Token de reset inválido"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            rateLimiterService.resetBucket(rateLimitKey);
            throw new TokenExpiredException("El token de reset ha expirado");
        }

        // Recargar el usuario desde la base de datos para obtener los cambios más recientes
        Usuario usuario = usuarioRepository.findById(resetToken.getUsuario().getId())
                .orElseThrow(() -> new InvalidTokenException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.newPassword()));
        usuarioRepository.save(usuario);

        // Eliminar el token usado
        passwordResetTokenRepository.delete(resetToken);
        rateLimiterService.resetBucket(rateLimitKey);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO createUser(CreateUserDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Usuario usuario = UsuarioMapper.toEntity(request);
        usuario.setPassword(passwordEncoder.encode(request.password()));

        Usuario savedUsuario = usuarioRepository.save(usuario);

        return UsuarioMapper.toDTO(savedUsuario);
    }
}
