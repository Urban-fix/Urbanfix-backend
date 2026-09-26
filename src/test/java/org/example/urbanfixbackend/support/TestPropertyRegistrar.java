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
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
    }
}
