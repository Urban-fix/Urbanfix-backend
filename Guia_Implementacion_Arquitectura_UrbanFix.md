# Guía Maestra de Implementación — UrbanFix Backend
Paquete base: `org.example.urbanfixbackend`
Ruta base: `src/main/java/org/example/urbanfixbackend/`
Estado: ✅ Spring Boot + Docker Compose + PostgreSQL configurados y corriendo. Actions pendiente (retomar en Fase 12).

---

## FASE 1 — Arquitectura de capas (estructura de paquetes)

Antes de escribir una sola entidad, crea toda la estructura de carpetas vacía. Esto evita que cada persona invente su propia organización en su rama.

Dentro de `src/main/java/org/example/urbanfixbackend/`, crea estos paquetes (carpetas):

```
├── config/          → Configuraciones de Spring (Security, CORS, Async, OpenAPI/Swagger opcional)
├── controller/       → Controladores REST (@RestController)
├── service/
│   ├── impl/         → Implementaciones de los servicios
├── repository/       → Interfaces Spring Data JPA
├── entity/            → Entidades JPA (@Entity)
├── dto/
│   ├── request/       → DTOs de entrada (Create/Update)
│   ├── response/      → DTOs de salida (Response/Detail)
├── exception/
│   ├── handler/       → GlobalExceptionHandler (@ControllerAdvice)
├── security/
│   ├── jwt/           → Filtros y utilidades de JWT
├── event/
│   ├── listener/      → Listeners de eventos (@EventListener)
├── mapper/            → Mappers manuales Entity ↔ DTO (o config de MapStruct)
└── UrbanfixBackendApplication.java  (ya existe, no tocar la raíz del paquete)
```

**Checklist Fase 1:**
- [ ] Crear las 14 carpetas listadas arriba (vacías está bien, Java/Git no trackean carpetas vacías — agrega un `.gitkeep` en cada una si quieres que se vean en el repo antes de tener archivos)
- [ ] Commit: `chore: estructura de paquetes en capas` → rama `feature/infra-calidad` → PR → merge a `develop`

---

## FASE 2 — Entidades JPA (Rúbrica: Entidades y modelo de datos, 3.0 pts)

Ruta: `src/main/java/org/example/urbanfixbackend/entity/`

Crea estos 7 archivos (las 3 entidades de fase 2 —Aporte, VerificacionResuelta, GrupoDuplicados— NO se crean todavía):

- [ ] `Usuario.java` — `@Entity`, campos: id, nombre, email (único), password (hasheada), rol (enum `Rol`: CIUDADANO, ADMIN_MUNICIPAL), fechaRegistro
- [ ] `Rol.java` — enum simple (puede ir en `entity/` o en un subpaquete `entity/enums/`)
- [ ] `Categoria.java` — id, nombre, descripcion
- [ ] `Zona.java` — id, nombre (barrio/distrito), coordenadasReferencia (opcional)
- [ ] `Reporte.java` — id, titulo, descripcion, latitud, longitud, fotoUrl, fechaCreacion, `@ManyToOne` a Usuario, `@ManyToOne` a Categoria, `@ManyToOne` a Zona, `@OneToMany` a EstadoHistorial, `@OneToMany` a Confirmacion, `@OneToMany` a Comentario, campo `estadoActual` (enum: REPORTADO, EN_PROCESO, RESUELTO)
- [ ] `EstadoHistorial.java` — id, `@ManyToOne` a Reporte, estadoAnterior, estadoNuevo, fechaCambio, `@ManyToOne` a Usuario (quién hizo el cambio)
- [ ] `Confirmacion.java` — id, `@ManyToOne` a Reporte, `@ManyToOne` a Usuario, fechaConfirmacion (constraint única: un usuario no puede confirmar 2 veces el mismo reporte — `@UniqueConstraint` en `@Table`)
- [ ] `Comentario.java` — id, `@ManyToOne` a Reporte, `@ManyToOne` a Usuario, contenido, fechaCreacion

**Checklist Fase 2:**
- [ ] Anotar correctamente `@Entity`, `@Table(name = "...")`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- [ ] Relaciones con `fetch = FetchType.LAZY` por defecto (evita el problema N+1)
- [ ] Validaciones a nivel de entidad: `@NotNull`, `@Email`, `@Size` donde aplique
- [ ] Levantar el proyecto y confirmar en los logs de Hibernate que las 7 tablas se crean correctamente (`spring.jpa.hibernate.ddl-auto=update`)
- [ ] Commit en rama correspondiente según quién implemente cada entidad (Persona 1 = Usuario/Rol, Persona 2 = Reporte/Categoria/Zona, Persona 3 = EstadoHistorial/Confirmacion/Comentario)

---

## FASE 3 — Repositorios (Spring Data JPA)

Ruta: `src/main/java/org/example/urbanfixbackend/repository/`

- [ ] `UsuarioRepository.java` — extends `JpaRepository<Usuario, Long>` + método `Optional<Usuario> findByEmail(String email)`
- [ ] `ReporteRepository.java` — extends `JpaRepository<Reporte, Long>` + métodos de filtro: `findByCategoriaId`, `findByZonaId`, `findByEstadoActual`
- [ ] `CategoriaRepository.java`
- [ ] `ZonaRepository.java`
- [ ] `EstadoHistorialRepository.java` — método `List<EstadoHistorial> findByReporteIdOrderByFechaCambioDesc`
- [ ] `ConfirmacionRepository.java` — método `boolean existsByReporteIdAndUsuarioId` (para evitar duplicados)
- [ ] `ComentarioRepository.java` — método `List<Comentario> findByReporteIdOrderByFechaCreacionAsc`

**Checklist Fase 3:**
- [ ] Los 7 repositorios creados y compilando
- [ ] Probar rápidamente (opcional, con un `CommandLineRunner` temporal) que se puede guardar y leer un registro de prueba

---

## FASE 4 — DTOs (Rúbrica: DTOs y mapeo, 2.0 pts — mínimo 10)

Ruta: `src/main/java/org/example/urbanfixbackend/dto/request/` y `dto/response/`

**Auth (Persona 1):**
- [ ] `request/RegisterRequestDTO.java` — nombre, email, password
- [ ] `request/LoginRequestDTO.java` — email, password
- [ ] `response/AuthResponseDTO.java` — accessToken, refreshToken, tipo de usuario

**Reportes (Persona 2):**
- [ ] `request/ReporteCreateDTO.java` — titulo, descripcion, latitud, longitud, categoriaId, zonaId, foto (multipart o URL)
- [ ] `request/ReporteUpdateDTO.java` — campos editables
- [ ] `response/ReporteResponseDTO.java` — versión resumida (para listados/mapa)
- [ ] `response/ReporteDetailDTO.java` — versión completa (incluye historial, confirmaciones, comentarios)

**Historial/Comunidad (Persona 3):**
- [ ] `request/CambioEstadoDTO.java` — nuevoEstado
- [ ] `response/EstadoHistorialResponseDTO.java`
- [ ] `request/ComentarioCreateDTO.java` — contenido
- [ ] `response/ComentarioResponseDTO.java`

Esto ya da 11 DTOs — por encima del mínimo de 10. Ninguno expone la entidad directamente ni datos sensibles (password nunca sale en ningún Response DTO).

**Checklist Fase 4:**
- [ ] Los 11 DTOs creados como `record` (recomendado en Java 21+) o clases con Lombok `@Data`
- [ ] Ningún DTO de respuesta expone `password` ni entidades JPA directamente

---

## FASE 5 — Mappers (Entity ↔ DTO)

Ruta: `src/main/java/org/example/urbanfixbackend/mapper/`

- [ ] `UsuarioMapper.java`
- [ ] `ReporteMapper.java`
- [ ] `EstadoHistorialMapper.java`
- [ ] `ComentarioMapper.java`

**Checklist Fase 5:**
- [ ] Decidir en equipo: mapeo manual (métodos estáticos `toDTO()`/`toEntity()`) o MapStruct (`@Mapper` + dependencia en pom.xml). Manual es más simple para no perder tiempo configurando MapStruct; MapStruct es más "profesional" si sobra tiempo.
- [ ] Cada mapper cubre las conversiones necesarias para su dominio

---

## FASE 6 — Excepciones personalizadas + Manejo global (Rúbrica: 2.0 pts — mínimo 7 excepciones)

Ruta: `src/main/java/org/example/urbanfixbackend/exception/`

- [ ] `UsuarioNotFoundException.java`
- [ ] `EmailAlreadyExistsException.java`
- [ ] `InvalidCredentialsException.java`
- [ ] `ReporteNotFoundException.java`
- [ ] `CategoriaNotFoundException.java`
- [ ] `ZonaNotFoundException.java`
- [ ] `InvalidCoordinatesException.java`
- [ ] `EstadoTransicionInvalidaException.java`
- [ ] `ConfirmacionDuplicadaException.java`
- [ ] `UnauthorizedActionException.java`

(10 excepciones, cómodamente por encima del mínimo de 7)

Ruta: `src/main/java/org/example/urbanfixbackend/exception/handler/`
- [ ] `GlobalExceptionHandler.java` — clase anotada `@RestControllerAdvice`, con un `@ExceptionHandler` por cada excepción de arriba, retornando un `ErrorResponseDTO` consistente (mensaje, código de estado, timestamp, path)
- [ ] `ErrorResponseDTO.java` (puede ir en `exception/` o en `dto/response/`)

**Checklist Fase 6:**
- [ ] Las 10 excepciones extienden `RuntimeException` con constructor que recibe mensaje
- [ ] `GlobalExceptionHandler` mapea cada una al código HTTP correcto (404 para NotFound, 409 para AlreadyExists/Duplicada, 400 para InvalidCoordinates/EstadoTransicionInvalida, 401/403 para InvalidCredentials/UnauthorizedAction)
- [ ] Probado manualmente con Postman: forzar cada error y confirmar que responde con el formato esperado

---

## FASE 7 — Seguridad: JWT + Roles (Rúbrica: 4.0 pts — la de mayor peso)

Ruta: `src/main/java/org/example/urbanfixbackend/security/`

- [ ] `SecurityConfig.java` — `@Configuration` + `@EnableWebSecurity`, define `SecurityFilterChain`, `PasswordEncoder` (BCrypt), CORS
- [ ] `security/jwt/JwtService.java` — generación y validación de tokens (access + refresh), extracción de claims
- [ ] `security/jwt/JwtAuthenticationFilter.java` — extends `OncePerRequestFilter`, intercepta cada request y valida el token
- [ ] `security/UserDetailsServiceImpl.java` — implementa `UserDetailsService`, carga el Usuario desde `UsuarioRepository`
- [ ] `security/CustomUserDetails.java` — implementa `UserDetails`, envuelve la entidad Usuario

Ruta: `src/main/java/org/example/urbanfixbackend/controller/`
- [ ] `AuthController.java` — endpoints:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/refresh`

Ruta: `src/main/java/org/example/urbanfixbackend/service/` y `service/impl/`
- [ ] `AuthService.java` (interfaz) + `AuthServiceImpl.java`

**Checklist Fase 7:**
- [ ] Registro funcional: password hasheada con BCrypt antes de guardar
- [ ] Login funcional: retorna accessToken + refreshToken
- [ ] `jwt.secret` y `jwt.expiration-access` leídos desde `application.properties` (ya están las variables en tu `.env`)
- [ ] Endpoints protegidos con `@PreAuthorize("hasRole('ADMIN_MUNICIPAL')")` donde corresponda (ej: solo admin puede cambiar estado de un reporte)
- [ ] Probado en Postman: login exitoso, acceso denegado sin token, acceso denegado con rol incorrecto

---

## FASE 8 — Services (lógica de negocio — Rúbrica: Arquitectura, 2.0 pts)

Ruta: `src/main/java/org/example/urbanfixbackend/service/` (interfaces) y `service/impl/` (implementaciones)

- [ ] `ReporteService.java` / `ReporteServiceImpl.java` — CRUD completo + lógica de filtros
- [ ] `EstadoHistorialService.java` / `EstadoHistorialServiceImpl.java` — lógica de transición de estados (validar que la transición sea válida: no se puede pasar de RESUELTO a REPORTADO, por ejemplo)
- [ ] `ConfirmacionService.java` / `ConfirmacionServiceImpl.java`
- [ ] `ComentarioService.java` / `ComentarioServiceImpl.java`

**Checklist Fase 8:**
- [ ] Cada Service inyectado por constructor (no `@Autowired` en campos — mejor práctica y facilita testing con Mockito después)
- [ ] Lógica de negocio vive en el Service, NO en el Controller (el controller solo recibe el DTO, llama al service, devuelve la respuesta)
- [ ] Lógica de negocio vive en el Service, NO en el Repository (el repository solo hace consultas)

---

## FASE 9 — Controllers REST (Rúbrica: API REST, 2.0 pts)

Ruta: `src/main/java/org/example/urbanfixbackend/controller/`

- [ ] `ReporteController.java`:
  - `POST /api/v1/reportes`
  - `GET /api/v1/reportes` (con query params: `?categoria=&zona=&estado=`)
  - `GET /api/v1/reportes/{id}`
  - `PUT /api/v1/reportes/{id}`
  - `DELETE /api/v1/reportes/{id}`
  - `PATCH /api/v1/reportes/{id}/estado`
- [ ] `ConfirmacionController.java`:
  - `POST /api/v1/reportes/{id}/confirmaciones`
- [ ] `ComentarioController.java`:
  - `POST /api/v1/reportes/{id}/comentarios`
  - `GET /api/v1/reportes/{id}/comentarios`
- [ ] `CategoriaController.java` / `ZonaController.java` (CRUD simple, probablemente solo lectura para el ciudadano, escritura solo admin)

**Checklist Fase 9:**
- [ ] URIs versionadas con `/api/v1/` en todas
- [ ] Verbos HTTP correctos (POST=crear, GET=leer, PUT=reemplazar, PATCH=actualización parcial, DELETE=eliminar)
- [ ] Códigos de estado correctos: 201 al crear, 200 al leer/actualizar, 204 al eliminar, 400/401/403/404/409 en errores
- [ ] Controllers "delgados" — máximo 3-5 líneas por método, toda la lógica delegada al Service

---

## FASE 10 — Eventos y Asincronía (Rúbrica: 2.0 pts)

Ruta: `src/main/java/org/example/urbanfixbackend/event/`
- [ ] `UsuarioRegistradoEvent.java` — record/clase simple con el Usuario recién creado
- [ ] `EstadoCambiadoEvent.java` — record/clase con el Reporte y el nuevo estado

Ruta: `src/main/java/org/example/urbanfixbackend/event/listener/`
- [ ] `EmailNotificationListener.java` — método `@EventListener` + `@Async` que escucha `UsuarioRegistradoEvent` y envía el email de bienvenida
- [ ] `EstadoNotificationListener.java` — método `@TransactionalEventListener` que escucha `EstadoCambiadoEvent` (se dispara solo si la transacción de BD se confirma)

Ruta: `src/main/java/org/example/urbanfixbackend/config/`
- [ ] `AsyncConfig.java` — `@Configuration` + `@EnableAsync`, define el `ThreadPoolTaskExecutor` personalizado
- [ ] `MailConfig.java` (solo si hace falta configuración adicional del `JavaMailSender`; Spring Boot suele autoconfigurar esto con las properties `spring.mail.*`)

Ruta: `src/main/java/org/example/urbanfixbackend/service/` (o `service/impl/`)
- [ ] `EmailService.java` / `EmailServiceImpl.java` — usa `JavaMailSender` para el envío real

**Checklist Fase 10:**
- [ ] `AuthServiceImpl` publica `UsuarioRegistradoEvent` después de guardar el usuario (usando `ApplicationEventPublisher`)
- [ ] `EstadoHistorialServiceImpl` publica `EstadoCambiadoEvent` después de guardar el cambio de estado
- [ ] Ambos listeners funcionando de forma asíncrona (verificar en logs que corren en un hilo distinto al principal)
- [ ] Servicio de email probado (MailHog local o cuenta de pruebas, según lo definido en `.env.example`)

---

## FASE 11 — Testing (JUnit, Mockito, TestContainers)

Ruta: `src/test/java/org/example/urbanfixbackend/`

- [ ] `service/ReporteServiceTest.java` — unitario con Mockito, mockeando `ReporteRepository`
- [ ] `service/AuthServiceTest.java` — unitario con Mockito
- [ ] `controller/ReporteControllerIntegrationTest.java` — integración con `@SpringBootTest` + TestContainers (patrón `@DynamicPropertySource`)
- [ ] Recrear `UrbanfixBackendApplicationTests.java` usando el patrón con TestContainers (el original se eliminó por el problema de conexión en CI)

**Checklist Fase 11:**
- [ ] Mínimo 1 test unitario por Service principal
- [ ] Mínimo 2-3 tests de integración con flujos completos (registro→login, crear reporte→cambiar estado)
- [ ] Todos los tests pasan localmente con `mvn clean verify`

---

## FASE 12 — Retomar GitHub Actions (pendiente desde antes)

- [ ] Restaurar `.github/workflows/ci.yml` (con el paso `actions/checkout@v4` primero, sin olvidarlo esta vez)
- [ ] Como ya existen tests reales con TestContainers, el runner `ubuntu-latest` ya trae Docker — no hace falta configuración extra
- [ ] Confirmar que corre en verde en un PR de prueba

---

## FASE 13 — Deployment

- [ ] Crear cuenta en Railway o Render
- [ ] Conectar el repositorio de GitHub
- [ ] Configurar las variables de entorno de producción (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, etc.) directamente en la plataforma — nunca en el código
- [ ] Provisionar una base de datos PostgreSQL en la nube (la mayoría de estas plataformas la ofrece integrada)
- [ ] Confirmar que la URL pública responde correctamente a un endpoint de prueba (ej. `GET /api/v1/categorias`)

---

## FASE 14 — Documentación final (Postman + README)

- [ ] `postman_collection.json` en la raíz del repo — todos los endpoints, con variables de entorno (`{{baseUrl}}`, `{{accessToken}}`) y ejemplos de request/response
- [ ] `README.md` completo (portada, índice, problema/solución, diagrama entidad-relación, arquitectura, manejo de errores, seguridad, eventos/asincronía, instalación, deployment, Git/GitHub, conclusión — 1000-2000 palabras)

---

## Checklist de cierre — antes de entregar

- [ ] Las 7 entidades del MVP implementadas y funcionando end-to-end
- [ ] Los 3 endpoints core (JWT+roles, CRUD reportes, transición de estados) probados manualmente en Postman
- [ ] Los 2 eventos asíncronos disparándose correctamente
- [ ] Mínimo 10 DTOs, mínimo 7 excepciones personalizadas
- [ ] Tests pasando localmente y en GitHub Actions
- [ ] Backend desplegado y accesible públicamente
- [ ] README y Postman Collection completos
- [ ] Todos los PRs de las 4 ramas `feature/*` mergeados a `develop`, y `develop` mergeado a `main`
