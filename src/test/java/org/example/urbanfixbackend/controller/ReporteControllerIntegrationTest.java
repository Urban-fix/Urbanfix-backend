package org.example.urbanfixbackend.controller;

import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.response.AuthResponseDTO;
import org.example.urbanfixbackend.dto.response.ReporteResponseDTO;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ReporteControllerIntegrationTest {

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
        registry.add("jwt.secret", () -> "test-secret-key-for-testing-purposes-only");
        registry.add("jwt.expiration-access", () -> "3600000");
        registry.add("jwt.expiration-refresh", () -> "86400000");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ZonaRepository zonaRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    private String baseUrl;
    private String accessToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        reporteRepository.deleteAll();
        usuarioRepository.deleteAll();
        categoriaRepository.deleteAll();
        zonaRepository.deleteAll();

        // Registrar usuario de prueba
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(
                "Test",
                "User",
                "test@example.com",
                "password123"
        );
        ResponseEntity<AuthResponseDTO> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                registerDTO,
                AuthResponseDTO.class
        );
        accessToken = registerResponse.getBody().accessToken();

        // Crear categoría y zona de prueba
        org.example.urbanfixbackend.entity.Categoria categoria = new org.example.urbanfixbackend.entity.Categoria();
        categoria.setNombre("Vialidad");
        categoria.setDescripcion("Problemas viales");
        categoriaRepository.save(categoria);

        org.example.urbanfixbackend.entity.Zona zona = new org.example.urbanfixbackend.entity.Zona();
        zona.setDistrito("Miraflores");
        zona.setCoordenadasReferencia("-12.119,-77.03");
        zonaRepository.save(zona);
    }

    @Test
    void createReporte_Success() {
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                1L,
                1L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<ReporteCreateDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<ReporteResponseDTO> response = restTemplate.exchange(
                baseUrl + "/reportes",
                HttpMethod.POST,
                request,
                ReporteResponseDTO.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bache en la calle", response.getBody().titulo());
    }

    @Test
    void getAllReportes_Success() {
        // Crear un reporte primero
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                1L,
                1L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<ReporteCreateDTO> createRequest = new HttpEntity<>(createDTO, headers);
        restTemplate.exchange(baseUrl + "/reportes", HttpMethod.POST, createRequest, ReporteResponseDTO.class);

        // Obtener todos los reportes
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<ReporteResponseDTO[]> response = restTemplate.exchange(
                baseUrl + "/reportes",
                HttpMethod.GET,
                getRequest,
                ReporteResponseDTO[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1);
    }

    @Test
    void getMisReportes_Success() {
        // Crear un reporte primero
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                1L,
                1L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<ReporteCreateDTO> createRequest = new HttpEntity<>(createDTO, headers);
        restTemplate.exchange(baseUrl + "/reportes", HttpMethod.POST, createRequest, ReporteResponseDTO.class);

        // Obtener mis reportes
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<ReporteResponseDTO[]> response = restTemplate.exchange(
                baseUrl + "/reportes/mis-reportes",
                HttpMethod.GET,
                getRequest,
                ReporteResponseDTO[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1);
    }

    @Test
    void loginAndCreateReporte_Flow() {
        // Login con el usuario registrado
        LoginRequestDTO loginDTO = new LoginRequestDTO("test@example.com", "password123");
        ResponseEntity<AuthResponseDTO> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginDTO,
                AuthResponseDTO.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        String token = loginResponse.getBody().accessToken();

        // Crear reporte con el token
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                1L,
                1L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<ReporteCreateDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<ReporteResponseDTO> response = restTemplate.exchange(
                baseUrl + "/reportes",
                HttpMethod.POST,
                request,
                ReporteResponseDTO.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
