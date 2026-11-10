package org.example.urbanfixbackend.support;

import org.springframework.test.context.DynamicPropertyRegistry;

public final class TestPropertyRegistrar {

    public static final String JWT_TEST_SECRET = "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=";

    private TestPropertyRegistrar() {
    }

    public static void registerJwtAndMail(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> JWT_TEST_SECRET);
        registry.add("jwt.expiration-access", () -> "3600000");
        registry.add("jwt.expiration-refresh", () -> "86400000");
        registry.add("password-reset.token-expiration-minutes", () -> "15");
        registry.add("email.enabled", () -> "false");
        registry.add("spring.mail.host", () -> "disabled");
        registry.add("APP_BASE_URL", () -> "http://localhost:8080");
    }
}
