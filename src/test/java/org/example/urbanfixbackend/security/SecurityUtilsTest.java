package org.example.urbanfixbackend.security;

import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilsTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_Success() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        usuario.setRol(Rol.CIUDADANO);

        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long userId = SecurityUtils.getCurrentUserId();

        assertEquals(1L, userId);
    }

    @Test
    void getCurrentUserId_NotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                SecurityUtils::getCurrentUserId);
        
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void getCurrentUserId_WrongPrincipalType() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("wrongType");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                SecurityUtils::getCurrentUserId);
        
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void getCurrentUserEmail_Success() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        usuario.setRol(Rol.CIUDADANO);

        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String email = SecurityUtils.getCurrentUserEmail();

        assertEquals("test@example.com", email);
    }

    @Test
    void getCurrentUserEmail_NotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                SecurityUtils::getCurrentUserEmail);
        
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void getCurrentUserEmail_WrongPrincipalType() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("wrongType");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                SecurityUtils::getCurrentUserEmail);
        
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void isAuthenticated_True() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");
        usuario.setRol(Rol.CIUDADANO);

        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(SecurityUtils.isAuthenticated());
    }

    @Test
    void isAuthenticated_False_NoAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    void isAuthenticated_False_NotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    void isAuthenticated_False_WrongPrincipalType() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("wrongType");
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(SecurityUtils.isAuthenticated());
    }
}
