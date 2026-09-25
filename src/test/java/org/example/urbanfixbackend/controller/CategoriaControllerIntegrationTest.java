package org.example.urbanfixbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.urbanfixbackend.dto.request.CategoriaCreateDTO;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
class CategoriaControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.email.enabled", () -> "false");
        registry.add("MAIL_HOST", () -> "localhost");
        registry.add("MAIL_PORT", () -> "3025");
        registry.add("MAIL_USERNAME", () -> "test");
        registry.add("MAIL_PASSWORD", () -> "test");
        registry.add("jwt.secret", () -> "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=");
        registry.add("jwt.expiration-access", () -> "3600000");
        registry.add("jwt.expiration-refresh", () -> "86400000");
        registry.add("password-reset.token-expiration-minutes", () -> "15");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Registrar usuario admin
        RegisterRequestDTO adminDTO = new RegisterRequestDTO(
                "Admin",
                "User",
                "admin@example.com",
                "Password123",
                Rol.ADMIN_MUNICIPAL
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isCreated());

        // Login como admin
        LoginRequestDTO loginDTO = new LoginRequestDTO("admin@example.com", "Password123");
        String adminLoginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminAccessToken = com.jayway.jsonpath.JsonPath.read(adminLoginResponse, "$.accessToken");
        assertNotNull(adminAccessToken);
    }

    @Test
    void createCategoria_Success() throws Exception {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Problemas viales");

        mockMvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Vialidad"))
                .andExpect(jsonPath("$.descripcion").value("Problemas viales"));
    }

    @Test
    void getAllCategorias_Success() throws Exception {
        // Crear categoría primero
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Problemas viales");
        mockMvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getCategoriaById_Success() throws Exception {
        // Crear categoría primero
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Problemas viales");
        String response = mockMvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        Long idLong = id.longValue();

        mockMvc.perform(get("/api/v1/categorias/" + idLong))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Vialidad"));
    }

    @Test
    void updateCategoria_Success() throws Exception {
        // Crear categoría primero
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Problemas viales");
        String response = mockMvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        Long idLong = id.longValue();

        CategoriaCreateDTO updateDTO = new CategoriaCreateDTO("Vialidad", "Nueva descripción");
        mockMvc.perform(put("/api/v1/categorias/" + idLong)
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Nueva descripción"));
    }

    @Test
    void deleteCategoria_Success() throws Exception {
        // Crear categoría primero
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Vialidad", "Problemas viales");
        String response = mockMvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        Long idLong = id.longValue();

        mockMvc.perform(delete("/api/v1/categorias/" + idLong)
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNoContent());
    }
}
