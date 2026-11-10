package org.example.urbanfixbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.urbanfixbackend.support.TestPropertyRegistrar;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.EstadoReporte;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class ReporteControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ZonaRepository zonaRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private String adminAccessToken;
    private Long categoriaId;
    private Long zonaId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        reporteRepository.deleteAll();
        usuarioRepository.deleteAll();
        categoriaRepository.deleteAll();
        zonaRepository.deleteAll();

        // Registrar usuario de prueba
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(
                "Test",
                "User",
                "test@example.com",
                "Password123"
        );

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        accessToken = JsonPath.read(registerResponse, "$.accessToken");

        // Crear usuario admin para pruebas de cambio de estado
        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("Admin1234"));
        admin.setRol(Rol.ADMIN_MUNICIPAL);
        usuarioRepository.save(admin);

        LoginRequestDTO adminLogin = new LoginRequestDTO("admin@example.com", "Admin1234");
        String adminLoginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminAccessToken = JsonPath.read(adminLoginResponse, "$.accessToken");

        // Crear categoría y zona de prueba
        org.example.urbanfixbackend.entity.Categoria categoria = new org.example.urbanfixbackend.entity.Categoria();
        categoria.setNombre("Vialidad");
        categoria.setDescripcion("Problemas viales");
        categoria = categoriaRepository.save(categoria);
        categoriaId = categoria.getId();

        org.example.urbanfixbackend.entity.Zona zona = new org.example.urbanfixbackend.entity.Zona();
        zona.setNombre("Miraflores");
        zona.setCoordenadasReferencia("-12.119,-77.03");
        zona = zonaRepository.save(zona);
        zonaId = zona.getId();
    }

    @Test
    void createReporte_Success() throws Exception {
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                categoriaId,
                zonaId
        );

        mockMvc.perform(post("/api/v1/reportes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Bache en la calle"));
    }

    @Test
    void getAllReportes_Success() throws Exception {
        // Crear un reporte primero
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                categoriaId,
                zonaId
        );

        mockMvc.perform(post("/api/v1/reportes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reportes")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getMisReportes_Success() throws Exception {
        // Crear un reporte primero
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                categoriaId,
                zonaId
        );

        mockMvc.perform(post("/api/v1/reportes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reportes/mis-reportes")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void loginAndCreateReporte_Flow() throws Exception {
        // Login con el usuario registrado
        LoginRequestDTO loginDTO = new LoginRequestDTO("test@example.com", "Password123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(loginResponse, "$.accessToken");
        assertNotNull(token);

        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                categoriaId,
                zonaId
        );

        mockMvc.perform(post("/api/v1/reportes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void cambiarEstadoReporte_AsAdmin_Success() throws Exception {
        // Crear un reporte primero
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                categoriaId,
                zonaId
        );

        String response = mockMvc.perform(post("/api/v1/reportes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reporteId = JsonPath.<Number>read(response, "$.id").longValue();

        mockMvc.perform(patch("/api/v1/reportes/" + reporteId + "/estado")
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(EstadoReporte.EN_PROCESO)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reportes/" + reporteId)
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoActual").value("EN_PROCESO"));
    }

    @Test
    void cambiarEstadoReporte_AsCiudadano_Forbidden() throws Exception {
        // Crear un reporte primero
        ReporteCreateDTO createDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande",
                -12.119,
                -77.03,
                "foto.jpg",
                categoriaId,
                zonaId
        );

        String response = mockMvc.perform(post("/api/v1/reportes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reporteId = JsonPath.<Number>read(response, "$.id").longValue();

        mockMvc.perform(patch("/api/v1/reportes/" + reporteId + "/estado")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(EstadoReporte.EN_PROCESO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReporteWithFile_Success() throws Exception {
        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "test-photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/reportes")
                .file(foto)
                .param("titulo", "Bache en la calle")
                .param("descripcion", "Hay un bache grande")
                .param("latitud", "-12.119")
                .param("longitud", "-77.03")
                .param("categoriaId", categoriaId.toString())
                .param("zonaId", zonaId.toString())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Bache en la calle"))
                .andExpect(jsonPath("$.fotoUrl").exists());
    }

    @Test
    void createReporteWithoutFile_Success() throws Exception {
        mockMvc.perform(multipart("/api/v1/reportes")
                .param("titulo", "Bache en la calle")
                .param("descripcion", "Hay un bache grande")
                .param("latitud", "-12.119")
                .param("longitud", "-77.03")
                .param("categoriaId", categoriaId.toString())
                .param("zonaId", zonaId.toString())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Bache en la calle"));
    }
}
