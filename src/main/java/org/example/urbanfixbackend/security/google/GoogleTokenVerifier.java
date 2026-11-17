package org.example.urbanfixbackend.security.google;

import org.example.urbanfixbackend.exception.InvalidTokenException;
import org.example.urbanfixbackend.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Valida el idToken que entrega "Sign in with Google" (frontend web y app móvil).
 *
 * Google verifica la firma del token en su endpoint oficial tokeninfo; aquí además se comprueba que:
 *  - el token fue emitido para alguno de nuestros Client ID (claim "aud"),
 *  - lo emitió Google (claim "iss"),
 *  - no ha expirado (claim "exp"),
 *  - el email está verificado por Google (claim "email_verified").
 *
 * Los Client ID se configuran en la variable de entorno GOOGLE_CLIENT_IDS (separados por coma).
 * Con el Web Client ID basta: la app móvil también pide el token con ese mismo ID.
 */
@Component
public class GoogleTokenVerifier {

    static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";
    static final Set<String> VALID_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final List<String> clientIds;
    private final RestClient restClient;

    public GoogleTokenVerifier(@Value("${google.client-ids:}") String clientIds) {
        this.clientIds = parseClientIds(clientIds);

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    /** true si hay al menos un Client ID configurado. */
    public boolean isConfigured() {
        return !clientIds.isEmpty();
    }

    /**
     * Verifica el idToken con Google y devuelve los datos del usuario.
     *
     * @throws InvalidTokenException si el token no es válido para esta aplicación
     * @throws ServiceException      si el login con Google no está configurado o Google no responde
     */
    public GoogleUserInfo verify(String idToken) {
        if (!isConfigured()) {
            throw new ServiceException("El inicio de sesión con Google no está configurado en el servidor (GOOGLE_CLIENT_IDS)");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new InvalidTokenException("Token de Google vacío");
        }
        return validate(fetchTokenInfo(idToken), clientIds, Instant.now());
    }

    /** Consulta tokeninfo. Google responde 400 si la firma o el formato del token no son válidos. */
    Map<String, Object> fetchTokenInfo(String idToken) {
        try {
            Map<String, Object> claims = restClient.get()
                    .uri(TOKENINFO_URL, idToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            if (claims == null) {
                throw new InvalidTokenException("Google no devolvió información del token");
            }
            return claims;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new InvalidTokenException("Token de Google inválido o expirado");
            }
            throw new ServiceException("No se pudo validar el token con Google", e);
        } catch (RestClientException e) {
            throw new ServiceException("No se pudo contactar a Google para validar el token", e);
        }
    }

    /** Reglas de validación de los claims (separadas para poder probarlas sin red). */
    static GoogleUserInfo validate(Map<String, Object> claims, List<String> allowedClientIds, Instant now) {
        String aud = asString(claims.get("aud"));
        if (aud == null || !allowedClientIds.contains(aud)) {
            throw new InvalidTokenException("El token de Google no fue emitido para esta aplicación");
        }

        String iss = asString(claims.get("iss"));
        if (iss == null || !VALID_ISSUERS.contains(iss)) {
            throw new InvalidTokenException("El token no fue emitido por Google");
        }

        String exp = asString(claims.get("exp"));
        try {
            if (exp == null || Instant.ofEpochSecond(Long.parseLong(exp)).isBefore(now)) {
                throw new InvalidTokenException("El token de Google expiró");
            }
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("El token de Google tiene una fecha de expiración inválida");
        }

        String email = asString(claims.get("email"));
        if (email == null || email.isBlank()) {
            throw new InvalidTokenException("La cuenta de Google no tiene email");
        }
        if (!"true".equalsIgnoreCase(asString(claims.get("email_verified")))) {
            throw new InvalidTokenException("El email de la cuenta de Google no está verificado");
        }

        return new GoogleUserInfo(
                asString(claims.get("sub")),
                email.trim().toLowerCase(),
                asString(claims.get("given_name")),
                asString(claims.get("family_name")),
                asString(claims.get("name"))
        );
    }

    static List<String> parseClientIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
