package org.example.urbanfixbackend.config;

import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en los controllers -- SIN ESTO, @PreAuthorize NO HACE NADA
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless con JWT, no necesita CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Protección contra clickjacking
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .contentTypeOptions(content -> content.disable())
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints específicos de auth primero (más específicos primero)
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/auth/admin/**").hasRole("ADMIN_MUNICIPAL")
                        // Endpoints públicos de auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/auth/password-reset/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        // Swagger: solo en desarrollo, deshabilitado en producción
                        // En producción, estos endpoints serán rechazados por el filtro JWT
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Actuator: health público, resto solo admin
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN_MUNICIPAL")
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/zonas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/{reporteId}/comentarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/{reporteId}/confirmaciones").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/{reporteId}/confirmaciones/conteo").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // /reportes/mis-reportes requiere autenticación
                        .requestMatchers("/api/v1/reportes/mis-reportes").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir localhost para desarrollo y dominio de producción
        String allowedOrigins = System.getenv().getOrDefault("ALLOWED_ORIGINS", "http://localhost:5173");
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
