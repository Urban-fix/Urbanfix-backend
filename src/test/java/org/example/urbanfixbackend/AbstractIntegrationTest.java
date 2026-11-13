package org.example.urbanfixbackend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.example.urbanfixbackend.security.RateLimiterService;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Base class for integration tests.
 *
 * Database isolation strategy:
 * - All integration tests share the same H2 in-memory database instance (Spring context caching)
 * - Each test class runs with @SpringBootTest, so the context is reused across classes
 * - To ensure isolation, we truncate ALL tables before EACH test method in @BeforeEach
 * - Tables are truncated in child-to-parent order to respect foreign key constraints
 * - RESTART IDENTITY resets sequences to prevent ID conflicts between test classes
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected RateLimiterService rateLimiterService;

    protected MockMvc mockMvc;

    /**
     * Sets up MockMvc before each test method.
     */
    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    /**
     * Cleans the database before each test method.
     * Deletes all rows in child-to-parent order to respect FK constraints.
     * Uses DELETE instead of TRUNCATE for H2 compatibility.
     */
    @BeforeEach
    void cleanDatabase() {
        // Clear rate limiter buckets
        rateLimiterService.resetBucket("login:admin@example.com");
        rateLimiterService.resetBucket("login:ciudadano@test.com");
        rateLimiterService.resetBucket("login:tecnico@test.com");
        rateLimiterService.resetBucket("login:supervisor@test.com");
        rateLimiterService.resetBucket("login:operador@test.com");
        rateLimiterService.resetBucket("login:auditor@test.com");
        rateLimiterService.resetBucket("login:admin@test.com");
        rateLimiterService.resetBucket("register:test@example.com");
        rateLimiterService.resetBucket("register:hacker@test.com");
        rateLimiterService.resetBucket("register:ratelimit@test.com");

        // Order: child tables first, then parent tables
        jdbcTemplate.execute("DELETE FROM comentarios");
        jdbcTemplate.execute("DELETE FROM confirmaciones");
        jdbcTemplate.execute("DELETE FROM estado_historial");
        jdbcTemplate.execute("DELETE FROM reportes");
        jdbcTemplate.execute("DELETE FROM categorias");
        jdbcTemplate.execute("DELETE FROM zonas");
        jdbcTemplate.execute("DELETE FROM usuarios");

        // Reset sequences for H2
        jdbcTemplate.execute("ALTER TABLE comentarios ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE confirmaciones ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE estado_historial ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reportes ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE categorias ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE zonas ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE usuarios ALTER COLUMN id RESTART WITH 1");
    }
}
