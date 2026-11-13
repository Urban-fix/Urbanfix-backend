package org.example.urbanfixbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.urbanfixbackend.AbstractIntegrationTest;
import org.example.urbanfixbackend.dto.request.CategoriaCreateDTO;
import org.example.urbanfixbackend.dto.request.ComentarioCreateDTO;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.request.ReporteCreateDTO;
import org.example.urbanfixbackend.dto.request.ZonaCreateDTO;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.repository.ComentarioRepository;
import org.example.urbanfixbackend.repository.ReporteRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ComentarioControllerIntegrationTest extends AbstractIntegrationTest {

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
    private ComentarioRepository comentarioRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String userAccessToken;
    private Long reporteId;

    @BeforeEach
    void setUp() throws Exception {
        // Crear usuario admin directamente en la base de datos con rol ADMIN_MUNICIPAL
        org.example.urbanfixbackend.entity.Usuario admin = new org.example.urbanfixbackend.entity.Usuario();
        admin.setNombre("Admin");
        admin.setApellido("User");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("Password123"));
        admin.setRol(org.example.urbanfixbackend.entity.enums.Rol.ADMIN_MUNICIPAL);
        admin.setFechaRegistro(java.time.LocalDateTime.now());
        usuarioRepository.save(admin);

        // Login como admin
        LoginRequestDTO loginDTO = new LoginRequestDTO("admin@example.com", "Password123");
        String userLoginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        userAccessToken = JsonPath.read(userLoginResponse, "$.accessToken");
        assertNotNull(userAccessToken);

        // Crear categoría
        CategoriaCreateDTO categoriaDTO = new CategoriaCreateDTO("Vialidad", "Problemas viales");
        String categoriaResponse = mockMvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number categoriaId = JsonPath.read(categoriaResponse, "$.id");
        Long categoriaIdLong = categoriaId.longValue();

        // Crear zona
        ZonaCreateDTO zonaDTO = new ZonaCreateDTO("Centro", "40.7128,-74.0060");
        String zonaResponse = mockMvc.perform(post("/api/v1/zonas")
                .header("Authorization", "Bearer " + userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zonaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number zonaId = JsonPath.read(zonaResponse, "$.id");
        Long zonaIdLong = zonaId.longValue();

        // Crear reporte
        ReporteCreateDTO reporteDTO = new ReporteCreateDTO(
                "Bache en la calle",
                "Hay un bache grande en la calle principal",
                40.7128,
                -74.0060,
                "http://example.com/photo.jpg",
                categoriaIdLong,
                zonaIdLong
        );
        String reporteResponse = mockMvc.perform(post("/api/v1/reportes")
                .header("Authorization", "Bearer " + userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reporteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(reporteResponse, "$.id");
        reporteId = id.longValue();
    }

    @Test
    void crearComentario_Success() throws Exception {
        ComentarioCreateDTO dto = new ComentarioCreateDTO("Este es un comentario de prueba");

        mockMvc.perform(post("/api/v1/reportes/" + reporteId + "/comentarios")
                .header("Authorization", "Bearer " + userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getComentariosByReporteId_Success() throws Exception {
        // Crear comentario primero
        ComentarioCreateDTO dto = new ComentarioCreateDTO("Este es un comentario de prueba");
        mockMvc.perform(post("/api/v1/reportes/" + reporteId + "/comentarios")
                .header("Authorization", "Bearer " + userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reportes/" + reporteId + "/comentarios"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarComentario_Success() throws Exception {
        // Crear comentario primero
        ComentarioCreateDTO dto = new ComentarioCreateDTO("Este es un comentario de prueba");
        String response = mockMvc.perform(post("/api/v1/reportes/" + reporteId + "/comentarios")
                .header("Authorization", "Bearer " + userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number comentarioId = JsonPath.read(response, "$.id");
        Long comentarioIdLong = comentarioId.longValue();

        mockMvc.perform(delete("/api/v1/reportes/" + reporteId + "/comentarios/" + comentarioIdLong)
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isNoContent());
    }
}
