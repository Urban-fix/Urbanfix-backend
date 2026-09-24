package org.example.urbanfixbackend.service;

import io.jsonwebtoken.JwtException;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.response.AuthResponseDTO;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.exception.EmailAlreadyExistsException;
import org.example.urbanfixbackend.exception.InvalidCredentialsException;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.security.CustomUserDetails;
import org.example.urbanfixbackend.security.jwt.JwtService;
import org.example.urbanfixbackend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private Usuario usuario;
    private RegisterRequestDTO registerDTO;
    private LoginRequestDTO loginDTO;
    
    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        usuario.setNombre("Test");
        usuario.setApellido("User");
        usuario.setPassword("encodedPassword");
        usuario.setRol(Rol.CIUDADANO);
        
        registerDTO = new RegisterRequestDTO(
                "Test",
                "User",
                "test@example.com",
                "password123"
        );
        
        loginDTO = new LoginRequestDTO(
                "test@example.com",
                "password123"
        );
    }
    
    @Test
    void register_Success() {
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refreshToken");
        
        AuthResponseDTO result = authService.register(registerDTO);
        
        assertNotNull(result);
        assertEquals("accessToken", result.accessToken());
        assertEquals("refreshToken", result.refreshToken());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }
    
    @Test
    void register_EmailAlreadyExists() {
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerDTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
    
    @Test
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refreshToken");
        
        AuthResponseDTO result = authService.login(loginDTO);
        
        assertNotNull(result);
        assertEquals("accessToken", result.accessToken());
        assertEquals("refreshToken", result.refreshToken());
        verify(authenticationManager, times(1)).authenticate(any());
    }
    
    @Test
    void login_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));
        
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginDTO));
    }
    
    @Test
    void refreshToken_Success() {
        when(jwtService.isRefreshToken("refreshToken")).thenReturn(true);
        when(jwtService.extractUsername("refreshToken")).thenReturn("test@example.com");
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(jwtService.isTokenValid("refreshToken", any(CustomUserDetails.class))).thenReturn(true);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("newRefreshToken");
        
        AuthResponseDTO result = authService.refreshToken("refreshToken");
        
        assertNotNull(result);
        assertEquals("newAccessToken", result.accessToken());
        assertEquals("newRefreshToken", result.refreshToken());
    }
    
    @Test
    void refreshToken_NotRefreshToken() {
        when(jwtService.isRefreshToken("accessToken")).thenReturn(false);
        
        assertThrows(org.example.urbanfixbackend.exception.InvalidTokenException.class, 
                () -> authService.refreshToken("accessToken"));
    }
    
    @Test
    void refreshToken_InvalidToken() {
        when(jwtService.isRefreshToken("invalidToken")).thenReturn(true);
        when(jwtService.extractUsername("invalidToken")).thenThrow(new JwtException("Invalid token"));
        
        assertThrows(org.example.urbanfixbackend.exception.InvalidTokenException.class, 
                () -> authService.refreshToken("invalidToken"));
    }
}
