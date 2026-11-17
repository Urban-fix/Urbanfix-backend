package org.example.urbanfixbackend.security.google;

/** Datos del usuario que vienen dentro de un idToken de Google ya validado. */
public record GoogleUserInfo(
        String subject,
        String email,
        String givenName,
        String familyName,
        String fullName
) {}
