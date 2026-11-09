package org.example.urbanfixbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.urbanfixbackend.entity.Categoria;
import org.example.urbanfixbackend.entity.Usuario;
import org.example.urbanfixbackend.entity.Zona;
import org.example.urbanfixbackend.entity.enums.Rol;
import org.example.urbanfixbackend.repository.CategoriaRepository;
import org.example.urbanfixbackend.repository.UsuarioRepository;
import org.example.urbanfixbackend.repository.ZonaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ZonaRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@urbanfix.com}")
    private String adminEmail;

    @Value("${admin.password:Admin123!}")
    private String adminPassword;

    @Value("${admin.nombre:Admin}")
    private String adminNombre;

    @Value("${admin.apellido:Municipal}")
    private String adminApellido;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            initializeCategorias();
        }
        if (zonaRepository.count() == 0) {
            initializeZonas();
        }
        initializeAdminUser();
    }

    private void initializeCategorias() {
        Categoria alumbrado = new Categoria();
        alumbrado.setNombre("Alumbrado Público");
        alumbrado.setDescripcion("Problemas con iluminación de calles, avenidas y espacios públicos");
        categoriaRepository.save(alumbrado);

        Categoria basura = new Categoria();
        basura.setNombre("Recolección de Basura");
        basura.setDescripcion("Problemas con la recolección de residuos y desperdicios");
        categoriaRepository.save(basura);

        Categoria calzadas = new Categoria();
        calzadas.setNombre("Calzadas y Aceras");
        calzadas.setDescripcion("Baches, grietas y deterioro de calles y veredas");
        categoriaRepository.save(calzadas);

        Categoria espacios = new Categoria();
        espacios.setNombre("Espacios Públicos");
        espacios.setDescripcion("Parques, plazas y áreas recreativas");
        categoriaRepository.save(espacios);

        Categoria senales = new Categoria();
        senales.setNombre("Señalización");
        senales.setDescripcion("Falta o deterioro de señales de tránsito y carteles informativos");
        categoriaRepository.save(senales);

        Categoria agua = new Categoria();
        agua.setNombre("Agua y Cloacas");
        agua.setDescripcion("Problemas con el suministro de agua y desagües");
        categoriaRepository.save(agua);

        log.info("Categorías iniciales creadas exitosamente");
    }

    private void initializeZonas() {
        Zona centro = new Zona();
        centro.setNombre("Centro");
        centro.setCoordenadasReferencia("-34.6037, -58.3816");
        zonaRepository.save(centro);

        Zona norte = new Zona();
        norte.setNombre("Zona Norte");
        norte.setCoordenadasReferencia("-34.5519, -58.4476");
        zonaRepository.save(norte);

        Zona sur = new Zona();
        sur.setNombre("Zona Sur");
        sur.setCoordenadasReferencia("-34.6457, -58.3960");
        zonaRepository.save(sur);

        Zona oeste = new Zona();
        oeste.setNombre("Zona Oeste");
        oeste.setCoordenadasReferencia("-34.6037, -58.5276");
        zonaRepository.save(oeste);

        Zona este = new Zona();
        este.setNombre("Zona Este");
        este.setCoordenadasReferencia("-34.6037, -58.2356");
        zonaRepository.save(este);

        log.info("Zonas iniciales creadas exitosamente");
    }

    private void initializeAdminUser() {
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario admin = new Usuario();
            admin.setNombre(adminNombre);
            admin.setApellido(adminApellido);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRol(Rol.ADMIN_MUNICIPAL);
            usuarioRepository.save(admin);
            log.info("Usuario admin inicial creado: {}", adminEmail);
            log.warn("IMPORTANTE: Cambia la contraseña del admin en producción usando las variables de entorno");
        } else {
            log.info("Usuario admin ya existe: {}", adminEmail);
        }
    }
}
