package org.example.urbanfixbackend.controller;

import jakarta.validation.Valid;
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
import org.example.urbanfixbackend.service.AuthService;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final Environment environment;

    public AuthController(AuthService authService, Environment environment) {
        this.authService = authService;
        this.environment = environment;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request, HttpServletResponse response) {
        var authResponse = authService.register(request);
        setAuthCookies(response, authResponse.accessToken(), authResponse.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletResponse response) {
        var authResponse = authService.login(request);
        setAuthCookies(response, authResponse.accessToken(), authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new RuntimeException("No refresh token found in cookies");
        }
        var authResponse = authService.refreshToken(refreshToken);
        setAuthCookies(response, authResponse.accessToken(), authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<PasswordResetResponseDTO> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        PasswordResetResponseDTO response = authService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/validate")
    public ResponseEntity<PasswordResetValidateResponseDTO> validatePasswordReset(@Valid @RequestBody PasswordResetValidateDTO request) {
        PasswordResetValidateResponseDTO response = authService.validatePasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDTO request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN_MUNICIPAL')")
    public ResponseEntity<UsuarioResponseDTO> createUser(@Valid @RequestBody CreateUserDTO request) {
        UsuarioResponseDTO response = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearAuthCookies(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getCurrentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var userDetails = (org.example.urbanfixbackend.security.CustomUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(org.example.urbanfixbackend.mapper.UsuarioMapper.toDTO(userDetails.getUsuario()));
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        boolean isSecure = isProduction();
        
        if (isSecure) {
            // Para HTTPS con SameSite=None (requerido para cross-origin cookies)
            response.addHeader("Set-Cookie", 
                String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None", 
                    "accessToken", accessToken, 15 * 60));
            response.addHeader("Set-Cookie", 
                String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None", 
                    "refreshToken", refreshToken, 7 * 24 * 60 * 60));
        } else {
            // Para desarrollo HTTP
            Cookie accessCookie = new Cookie("accessToken", accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(15 * 60);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(refreshCookie);
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        boolean isSecure = isProduction();
        
        if (isSecure) {
            // Para HTTPS con SameSite=None
            response.addHeader("Set-Cookie", 
                "accessToken=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None");
            response.addHeader("Set-Cookie", 
                "refreshToken=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None");
        } else {
            // Para desarrollo HTTP
            Cookie accessCookie = new Cookie("accessToken", "");
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(0);
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("refreshToken", "");
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(0);
            response.addCookie(refreshCookie);
        }
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(org.springframework.core.env.Profiles.of("prod"));
    }
}
