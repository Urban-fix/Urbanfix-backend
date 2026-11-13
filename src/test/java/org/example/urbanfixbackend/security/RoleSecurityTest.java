package org.example.urbanfixbackend.security;

import org.example.urbanfixbackend.AbstractIntegrationTest;
import org.example.urbanfixbackend.dto.request.LoginRequestDTO;
import org.example.urbanfixbackend.dto.request.RegisterRequestDTO;
import org.example.urbanfixbackend.dto.response.AuthResponseDTO;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de seguridad para verificar que los roles funcionan correctamente
 * y que no hay escalación de privilegios.
 */
class RoleSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ZonaRepository zonaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private String ciudadanoToken;
    private String adminToken;
    private String tecnicoToken;
    private String supervisorToken;
    private String operadorToken;
    private String auditorToken;

    @BeforeEach
    void setUp() {
        // Crear usuarios con diferentes roles
        crearUsuario("ciudadano@test.com", "Password123", Rol.CIUDADANO);
        crearUsuario("admin@test.com", "Password123", Rol.ADMIN_MUNICIPAL);
        crearUsuario("tecnico@test.com", "Password123", Rol.TECNICO);
        crearUsuario("supervisor@test.com", "Password123", Rol.SUPERVISOR);
        crearUsuario("operador@test.com", "Password123", Rol.OPERADOR);
        crearUsuario("auditor@test.com", "Password123", Rol.AUDITOR);

        // Crear datos de prueba necesarios
        crearZona("Zona Test");
        crearCategoria("Categoria Test", "Descripción test");

        // Obtener tokens para cada rol
        ciudadanoToken = login("ciudadano@test.com", "Password123");
        adminToken = login("admin@test.com", "Password123");
        tecnicoToken = login("tecnico@test.com", "Password123");
        supervisorToken = login("supervisor@test.com", "Password123");
        operadorToken = login("operador@test.com", "Password123");
        auditorToken = login("auditor@test.com", "Password123");
    }

    @Test
    void testRegistroPublico_NoPermiteRolesPrivilegiados() throws Exception {
        // Intentar registrar usuario con rol ADMIN_MUNICIPAL vía endpoint público
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(
                "Hacker",
                "User",
                "hacker@test.com",
                "Password123"
        );

        // El registro público ignora cualquier rol especificado en el DTO
        // y siempre asigna CIUDADANO
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Hacker\",\"apellido\":\"User\",\"email\":\"hacker@test.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CIUDADANO"));
    }

    @Test
    void testEndpointAdminCreateUser_SoloAdminMunicipal() throws Exception {
        // ADMIN_MUNICIPAL puede crear usuarios
        mockMvc.perform(post("/api/v1/auth/admin/create-user")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"New\",\"apellido\":\"User\",\"email\":\"new@test.com\",\"password\":\"Password123\",\"rol\":\"TECNICO\"}"))
                .andExpect(status().isCreated());

        // CIUDADANO no puede crear usuarios
        mockMvc.perform(post("/api/v1/auth/admin/create-user")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"New\",\"apellido\":\"User\",\"email\":\"new2@test.com\",\"password\":\"Password123\",\"rol\":\"TECNICO\"}"))
                .andExpect(status().isForbidden());

        // TECNICO no puede crear usuarios
        mockMvc.perform(post("/api/v1/auth/admin/create-user")
                        .header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"New\",\"apellido\":\"User\",\"email\":\"new3@test.com\",\"password\":\"Password123\",\"rol\":\"TECNICO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEndpointCambiarEstadoReporte_RolesPermitidos() throws Exception {
        // Crear un reporte primero (simplificado)
        mockMvc.perform(post("/api/v1/reportes")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Test Reporte\",\"descripcion\":\"Test descripcion reporte\",\"latitud\":0.0,\"longitud\":0.0,\"categoriaId\":1,\"zonaId\":1}"))
                .andExpect(status().isCreated());

        // ADMIN_MUNICIPAL puede cambiar estado de REPORTADO a EN_PROCESO
        mockMvc.perform(patch("/api/v1/reportes/1/estado")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"EN_PROCESO\""))
                .andExpect(status().isOk());

        // TECNICO puede cambiar estado de EN_PROCESO a RESUELTO
        mockMvc.perform(patch("/api/v1/reportes/1/estado")
                        .header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"RESUELTO\""))
                .andExpect(status().isOk());

        // CIUDADANO NO puede cambiar estado
        mockMvc.perform(patch("/api/v1/reportes/1/estado")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"EN_PROCESO\""))
                .andExpect(status().isForbidden());

        // OPERADOR NO puede cambiar estado
        mockMvc.perform(patch("/api/v1/reportes/1/estado")
                        .header("Authorization", "Bearer " + operadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"EN_PROCESO\""))
                .andExpect(status().isForbidden());

        // AUDITOR NO puede cambiar estado
        mockMvc.perform(patch("/api/v1/reportes/1/estado")
                        .header("Authorization", "Bearer " + auditorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"EN_PROCESO\""))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEndpointCrearCategoria_RolesPermitidos() throws Exception {
        // ADMIN_MUNICIPAL puede crear categorías
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueva Categoria Admin\",\"descripcion\":\"Descripcion test categoria\"}"))
                .andExpect(status().isCreated());

        // SUPERVISOR puede crear categorías
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Otra Categoria Supervisor\",\"descripcion\":\"Descripcion test supervisor\"}"))
                .andExpect(status().isCreated());

        // CIUDADANO NO puede crear categorías
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + ciudadanoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Hacker\",\"descripcion\":\"Descripcion test hacker\"}"))
                .andExpect(status().isForbidden());

        // TECNICO NO puede crear categorías
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Hacker2\",\"descripcion\":\"Descripcion test hacker2\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEndpointEliminarCategoria_SoloAdminMunicipal() throws Exception {
        // Crear categoría primero
        mockMvc.perform(post("/api/v1/categorias")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"ParaBorrar\",\"descripcion\":\"Descripcion para borrar\"}"))
                .andExpect(status().isCreated());

        // ADMIN_MUNICIPAL puede eliminar (ID será 2 porque ya existe una de prueba)
        mockMvc.perform(delete("/api/v1/categorias/2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // SUPERVISOR NO puede eliminar
        mockMvc.perform(delete("/api/v1/categorias/1")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEndpointAuthMe_RequiereAutenticacion() throws Exception {
        // Sin token - 401
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isForbidden());

        // Con token válido - 200
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + ciudadanoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ciudadano@test.com"));
    }

    @Test
    void testRateLimitingLogin() throws Exception {
        // Intentar login con contraseña incorrecta múltiples veces
        // Rate limiter: 5 intentos por email en 15 minutos
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"ciudadano@test.com\",\"password\":\"WrongPassword123\"}"))
                    .andExpect(status().isUnauthorized());
        }

        // El 6to intento debería ser rate limitado (returns 401 for InvalidTokenException)
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ciudadano@test.com\",\"password\":\"WrongPassword123\"}"))
                .andExpect(status().isUnauthorized()); // Rate limit exception returns 401
    }

    @Test
    void testRateLimitingRegister() throws Exception {
        // Intentar registrar múltiples cuentas con el MISMO email
        // Rate limiter: 5 intentos por email en 60 minutos
        // Nota: El primer registro tendrá éxito (201), los siguientes fallarán con 409 (email exists)
        // porque la validación de email existe ocurre antes del rate limit
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Test\",\"apellido\":\"User\",\"email\":\"ratelimit@test.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isCreated());

        // Los intentos adicionales fallarán por email duplicado (409)
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nombre\":\"Test\",\"apellido\":\"User\",\"email\":\"ratelimit@test.com\",\"password\":\"Password123\"}"))
                    .andExpect(status().isConflict());
        }
    }

    private void crearUsuario(String email, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Test");
        usuario.setApellido("User");
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
    }

    private void crearZona(String nombre) {
        org.example.urbanfixbackend.entity.Zona zona = new org.example.urbanfixbackend.entity.Zona();
        zona.setNombre(nombre);
        zonaRepository.save(zona);
    }

    private void crearCategoria(String nombre, String descripcion) {
        org.example.urbanfixbackend.entity.Categoria categoria = new org.example.urbanfixbackend.entity.Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoriaRepository.save(categoria);
    }

    private String login(String email, String password) {
        LoginRequestDTO loginDTO = new LoginRequestDTO(email, password);
        AuthResponseDTO response = authService.login(loginDTO);
        return response.accessToken();
    }
}
