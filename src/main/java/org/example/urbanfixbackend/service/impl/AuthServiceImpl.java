package org.example.urbanfixbackend.service.impl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.response.AuthResponseDTO;
import org.example.urbanfixbackend.dto.response.UsuarioResponseDTO;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.exception.EmailAlreadyExistsException;
import org.example.urbanfixbackend.exception.InvalidCredentialsException;
import org.example.urbanfixbackend.exception.InvalidTokenException;
import org.example.urbanfixbackend.exception.TokenExpiredException;
import org.example.urbanfixbackend.mapper.UsuarioMapper;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.security.CustomUserDetails;
import org.example.urbanfixbackend.security.jwt.JwtService;
import org.example.urbanfixbackend.event.UsuarioRegistradoEvent;
import org.example.urbanfixbackend.service.AuthService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

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
}
