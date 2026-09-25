package org.example.urbanfixbackend.security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.urbanfixbackend.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("DEBUG JWT Filter: Processing request: " + request.getMethod() + " " + request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");
        System.out.println("DEBUG JWT Filter: Auth header: " + authHeader);
        System.out.println("DEBUG JWT Filter: Starts with Bearer? " + (authHeader != null && authHeader.startsWith("Bearer ")));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("DEBUG JWT Filter: No Bearer token, continuing chain");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (!jwtService.isAccessToken(jwt)) {
                System.out.println("DEBUG JWT: Not an access token");
                filterChain.doFilter(request, response);
                return;
            }

            final String userEmail = jwtService.extractUsername(jwt);
            System.out.println("DEBUG JWT: Extracted email: " + userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("DEBUG JWT: Loaded user details, authorities: " + userDetails.getAuthorities());

                if (jwtService.isTokenValid(jwt, (CustomUserDetails) userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("DEBUG JWT: Authentication set successfully");
                } else {
                    System.out.println("DEBUG JWT: Token validation failed");
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            System.out.println("DEBUG JWT: Exception - " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}