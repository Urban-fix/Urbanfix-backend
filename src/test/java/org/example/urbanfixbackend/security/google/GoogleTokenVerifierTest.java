package org.example.urbanfixbackend.security.google;

import org.example.urbanfixbackend.exception.InvalidTokenException;
import org.example.urbanfixbackend.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoogleTokenVerifierTest {

    private static final String CLIENT_ID = "123-abc.apps.googleusercontent.com";
    private static final Instant NOW = Instant.ofEpochSecond(1_800_000_000L);

    private Map<String, Object> claimsValidos() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", CLIENT_ID);
        claims.put("iss", "https://accounts.google.com");
        claims.put("exp", String.valueOf(NOW.getEpochSecond() + 600));
        claims.put("sub", "1098765");
        claims.put("email", "Vecino.Prueba@gmail.com");
        claims.put("email_verified", "true");
        claims.put("given_name", "Vecino");
        claims.put("family_name", "Prueba");
        claims.put("name", "Vecino Prueba");
        return claims;
    }

    @Test
    void validate_TokenValido_DevuelveDatosDelUsuario() {
        GoogleUserInfo info = GoogleTokenVerifier.validate(claimsValidos(), List.of(CLIENT_ID), NOW);

        assertEquals("vecino.prueba@gmail.com", info.email());
        assertEquals("Vecino", info.givenName());
        assertEquals("Prueba", info.familyName());
        assertEquals("1098765", info.subject());
    }

    @Test
    void validate_IssuerSinHttps_EsValido() {
        Map<String, Object> claims = claimsValidos();
        claims.put("iss", "accounts.google.com");

        assertDoesNotThrow(() -> GoogleTokenVerifier.validate(claims, List.of(CLIENT_ID), NOW));
    }

    @Test
    void validate_AudienceDeOtraApp_Falla() {
        Map<String, Object> claims = claimsValidos();
        claims.put("aud", "otra-app.apps.googleusercontent.com");

        assertThrows(InvalidTokenException.class, () -> GoogleTokenVerifier.validate(claims, List.of(CLIENT_ID), NOW));
    }

    @Test
    void validate_IssuerQueNoEsGoogle_Falla() {
        Map<String, Object> claims = claimsValidos();
        claims.put("iss", "https://evil.example.com");

        assertThrows(InvalidTokenException.class, () -> GoogleTokenVerifier.validate(claims, List.of(CLIENT_ID), NOW));
    }

    @Test
    void validate_TokenExpirado_Falla() {
        Map<String, Object> claims = claimsValidos();
        claims.put("exp", String.valueOf(NOW.getEpochSecond() - 1));

        assertThrows(InvalidTokenException.class, () -> GoogleTokenVerifier.validate(claims, List.of(CLIENT_ID), NOW));
    }

    @Test
    void validate_EmailNoVerificado_Falla() {
        Map<String, Object> claims = claimsValidos();
        claims.put("email_verified", "false");

        assertThrows(InvalidTokenException.class, () -> GoogleTokenVerifier.validate(claims, List.of(CLIENT_ID), NOW));
    }

    @Test
    void validate_SinEmail_Falla() {
        Map<String, Object> claims = claimsValidos();
        claims.remove("email");

        assertThrows(InvalidTokenException.class, () -> GoogleTokenVerifier.validate(claims, List.of(CLIENT_ID), NOW));
    }

    @Test
    void parseClientIds_SeparaPorComaEIgnoraVacios() {
        assertEquals(List.of("a", "b"), GoogleTokenVerifier.parseClientIds(" a , ,b "));
        assertTrue(GoogleTokenVerifier.parseClientIds("").isEmpty());
        assertTrue(GoogleTokenVerifier.parseClientIds(null).isEmpty());
    }

    @Test
    void verify_SinClientIdsConfigurados_Falla() {
        GoogleTokenVerifier verifier = new GoogleTokenVerifier("");

        assertFalse(verifier.isConfigured());
        assertThrows(ServiceException.class, () -> verifier.verify("cualquier-token"));
    }

    @Test
    void verify_TokenVacio_Falla() {
        GoogleTokenVerifier verifier = new GoogleTokenVerifier(CLIENT_ID);

        assertThrows(InvalidTokenException.class, () -> verifier.verify(" "));
    }
}
