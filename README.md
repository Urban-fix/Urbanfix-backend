# 🏙️ UrbanFix Backend

Backend REST para **UrbanFix**, una plataforma orientada a la gestión colaborativa de incidencias urbanas.

UrbanFix permite que los ciudadanos reporten problemas de infraestructura o servicios públicos —como baches, fallas de alumbrado, acumulación de residuos o problemas en espacios públicos— y proporciona herramientas para que personal municipal pueda gestionar, supervisar y actualizar el estado de estos reportes.

El backend está desarrollado con **Java 21**, **Spring Boot**, **Spring Security**, **Spring Data JPA** y **PostgreSQL**, siguiendo una arquitectura por capas y utilizando autenticación mediante **JWT**.

---

## 📑 Tabla de contenidos

- [Objetivo del proyecto](#-objetivo-del-proyecto)
- [Características principales](#-características-principales)
- [Tecnologías](#-tecnologías)
- [Arquitectura](#-arquitectura)
- [Modelo de datos](#-modelo-de-datos)
- [Seguridad y autenticación](#-seguridad-y-autenticación)
- [Roles](#-roles)
- [Estados de un reporte](#-estados-de-un-reporte)
- [API REST](#-api-rest)
- [Variables de entorno](#-variables-de-entorno)
- [Instalación](#-instalación)
- [Ejecución con Docker](#-ejecución-con-docker)
- [Ejecución del backend](#-ejecución-del-backend)
- [Pruebas](#-pruebas)
- [Postman](#-postman)
- [Eventos y notificaciones](#-eventos-y-notificaciones)
- [CI/CD](#-cicd)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Flujo de trabajo con Git](#-flujo-de-trabajo-con-git)
- [Estado actual](#-estado-actual)

---

# 🎯 Objetivo del proyecto

En muchas ciudades existen problemas cotidianos como calles deterioradas, falta de iluminación, acumulación de basura o daños en espacios públicos.

Sin una plataforma centralizada, los ciudadanos pueden tener dificultades para informar estos problemas y las autoridades municipales pueden tener dificultades para priorizarlos y hacer seguimiento de su resolución.

**UrbanFix** busca centralizar este proceso permitiendo:

- registrar incidencias urbanas;
- ubicar cada reporte mediante coordenadas;
- clasificarlos por categoría y zona;
- consultar reportes existentes;
- confirmar problemas reportados por otros ciudadanos;
- agregar comentarios;
- mantener un historial de cambios de estado;
- gestionar los reportes mediante distintos roles;
- notificar determinados eventos mediante correo electrónico.

---

# ✨ Características principales

Actualmente el backend incluye:

- Registro e inicio de sesión de usuarios.
- Autenticación mediante JWT.
- Access Token y Refresh Token.
- Recuperación de contraseña.
- Control de acceso basado en roles.
- Creación, consulta, edición y eliminación de reportes.
- Búsqueda de reportes.
- Filtrado por:
  - estado;
  - categoría;
  - zona;
  - usuario.
- Consulta de reportes creados por el usuario autenticado.
- Gestión de categorías.
- Gestión de zonas.
- Confirmación de reportes por ciudadanos.
- Prevención de confirmaciones duplicadas.
- Comentarios asociados a reportes.
- Historial de cambios de estado.
- Notificaciones mediante eventos asíncronos.
- Envío o generación de correos electrónicos.
- Validación de DTOs.
- Manejo centralizado de excepciones.
- Pruebas unitarias.
- Pruebas de integración.
- PostgreSQL mediante Docker Compose.
- Colección de Postman.
- Integración continua con GitHub Actions.
- Generación automática de artefactos `.jar`.

---

# 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal |
| Spring Boot 4 | Framework backend |
| Spring Web MVC | API REST |
| Spring Data JPA | Acceso y persistencia de datos |
| Hibernate | ORM |
| Spring Security | Seguridad |
| JWT / JJWT | Autenticación basada en tokens |
| PostgreSQL 16 | Base de datos |
| Docker / Docker Compose | Base de datos local |
| Maven | Gestión de dependencias y build |
| Lombok | Reducción de código repetitivo |
| Jakarta Validation | Validación de solicitudes |
| Spring Mail | Envío de correos |
| Thymeleaf | Templates HTML para emails |
| JUnit | Pruebas |
| Mockito | Mocking en pruebas unitarias |
| Testcontainers | Pruebas de integración |
| JaCoCo | Cobertura de pruebas |
| GitHub Actions | Integración continua |

---

# 🏗️ Arquitectura

El proyecto utiliza una **arquitectura por capas**, separando las responsabilidades del sistema.

```text
HTTP Request
     │
     ▼
Controller
     │
     ▼
Service
     │
     ▼
Repository
     │
     ▼
PostgreSQL
```

Además, los datos intercambiados mediante la API utilizan DTOs para evitar exponer directamente las entidades persistentes.

```text
Request
   │
   ▼
Request DTO
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Mapper
   │
   ▼
Entity
   │
   ▼
Repository
```

Esta separación facilita el mantenimiento, las pruebas y la evolución del sistema.

---

# 🗃️ Modelo de datos

Las principales entidades del sistema son:

### Usuario

Representa una persona registrada en UrbanFix.

Contiene información como:

- nombre;
- apellido;
- email;
- contraseña cifrada;
- rol;
- fecha de registro.

### Reporte

Representa una incidencia urbana reportada.

Incluye:

- título;
- descripción;
- latitud;
- longitud;
- URL de fotografía;
- estado actual;
- usuario creador;
- categoría;
- zona;
- fecha de creación.

### Categoria

Permite clasificar los reportes según el tipo de problema urbano.

Ejemplos iniciales:

- Alumbrado Público;
- Recolección de Basura;
- Calzadas y Aceras;
- Espacios Públicos;
- Señalización;
- Agua y Cloacas.

### Zona

Permite agrupar reportes geográficamente.

El backend crea automáticamente algunas zonas iniciales cuando la base de datos se encuentra vacía.

### EstadoHistorial

Registra los cambios de estado de un reporte.

Permite conocer:

- estado anterior;
- estado nuevo;
- usuario que realizó el cambio;
- fecha del cambio.

### Confirmacion

Permite que otro usuario confirme que un problema reportado realmente existe.

Existe una restricción para evitar que el mismo usuario confirme dos veces el mismo reporte.

### Comentario

Permite agregar comentarios asociados a un reporte.

### PasswordResetToken

Almacena temporalmente los tokens utilizados durante el proceso de recuperación de contraseña.

---

# 🔐 Seguridad y autenticación

UrbanFix utiliza **Spring Security + JWT**.

El flujo general es:

```text
Usuario
  │
  │ POST /login
  ▼
Backend
  │
  ├── valida credenciales
  │
  ├── genera Access Token
  │
  └── genera Refresh Token
          │
          ▼
       Cliente
```

Para acceder a un endpoint protegido debe enviarse:

```http
Authorization: Bearer <access_token>
```

Las contraseñas son almacenadas utilizando **BCrypt**.

La aplicación utiliza sesiones **stateless**, por lo que el servidor no mantiene una sesión HTTP tradicional de cada usuario.

---

# 👥 Roles

Actualmente existen los siguientes roles:

```text
CIUDADANO
ADMIN_MUNICIPAL
TECNICO
SUPERVISOR
OPERADOR
AUDITOR
```

Los permisos dependen del endpoint.

Por ejemplo:

- las consultas de reportes, categorías y zonas pueden ser públicas;
- crear reportes requiere autenticación;
- modificar determinados recursos requiere roles administrativos;
- cambiar el estado de un reporte está permitido para:

```text
ADMIN_MUNICIPAL
TECNICO
SUPERVISOR
```

---

# 🚦 Estados de un reporte

Un reporte puede encontrarse en alguno de los siguientes estados:

```text
REPORTADO
EN_PROCESO
RESUELTO
RECHAZADO
```

Cada modificación genera un registro en el historial del reporte.

---

# 🌐 API REST

La API se encuentra versionada bajo:

```text
/api/v1
```

## Autenticación

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registrar usuario |
| POST | `/api/v1/auth/login` | Iniciar sesión |
| POST | `/api/v1/auth/refresh` | Renovar tokens |
| POST | `/api/v1/auth/password-reset/request` | Solicitar recuperación de contraseña |
| POST | `/api/v1/auth/password-reset/confirm` | Confirmar nueva contraseña |

---

## Reportes

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/reportes` | Crear reporte |
| GET | `/api/v1/reportes` | Listar reportes |
| GET | `/api/v1/reportes/{id}` | Obtener reporte |
| GET | `/api/v1/reportes/{id}/detalle` | Obtener detalle completo |
| GET | `/api/v1/reportes/mis-reportes` | Obtener reportes del usuario autenticado |
| PUT | `/api/v1/reportes/{id}` | Actualizar reporte |
| DELETE | `/api/v1/reportes/{id}` | Eliminar reporte |
| PATCH | `/api/v1/reportes/{id}/estado` | Cambiar estado |

Los reportes también admiten filtros mediante query parameters.

Ejemplos:

```http
GET /api/v1/reportes?estado=REPORTADO
```

```http
GET /api/v1/reportes?categoriaId=1
```

```http
GET /api/v1/reportes?zonaId=2
```

```http
GET /api/v1/reportes?usuarioId=5
```

```http
GET /api/v1/reportes?search=bache
```

---

## Categorías

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/categorias` | Crear categoría |
| GET | `/api/v1/categorias` | Listar categorías |
| GET | `/api/v1/categorias/{id}` | Obtener categoría |
| PUT | `/api/v1/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/v1/categorias/{id}` | Eliminar categoría |

---

## Zonas

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/zonas` | Crear zona |
| GET | `/api/v1/zonas` | Listar zonas |
| GET | `/api/v1/zonas/{id}` | Obtener zona |
| PUT | `/api/v1/zonas/{id}` | Actualizar zona |
| DELETE | `/api/v1/zonas/{id}` | Eliminar zona |

---

## Confirmaciones

Base:

```text
/api/v1/reportes/{reporteId}/confirmaciones
```

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/` | Confirmar reporte |
| DELETE | `/` | Eliminar confirmación |
| GET | `/` | Usuarios que confirmaron |
| GET | `/conteo` | Número de confirmaciones |
| GET | `/verificar` | Verificar si el usuario confirmó |

---

## Comentarios

Base:

```text
/api/v1/reportes/{reporteId}/comentarios
```

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/` | Crear comentario |
| GET | `/` | Listar comentarios |
| DELETE | `/{comentarioId}` | Eliminar comentario |

---

# ⚙️ Variables de entorno

El repositorio contiene:

```text
.env.example
```

Primero se debe crear un archivo `.env`.

### Linux / macOS

```bash
cp .env.example .env
```

### Windows PowerShell

```powershell
Copy-Item .env.example .env
```

Variables principales:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=urbanfix
DB_USER=urbanfix_user
DB_PASSWORD=urbanfix_local_pass

JWT_SECRET=your_base64_encoded_jwt_secret
JWT_EXPIRATION_ACCESS=900000
JWT_EXPIRATION_REFRESH=604800000

PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES=15

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
```

> ⚠️ Nunca se deben subir credenciales reales al repositorio.

Para generar una clave JWT segura puede utilizarse:

```bash
openssl rand -base64 32
```

---

# 📦 Instalación

## 1. Clonar el repositorio

```bash
git clone https://github.com/Urban-fix/Urbanfix-backend.git
```

Entrar al proyecto:

```bash
cd Urbanfix-backend
```

El desarrollo principal se realiza sobre la rama:

```bash
git checkout develop
```

---

# 🐳 Ejecución con Docker

UrbanFix utiliza PostgreSQL 16 mediante Docker Compose.

Primero debe existir el archivo `.env`.

Luego ejecutar:

```bash
docker compose up -d
```

Comprobar los contenedores:

```bash
docker ps
```

Debería aparecer:

```text
urbanfix-db
```

Para detener la base de datos:

```bash
docker compose down
```

Para eliminar también el volumen de datos:

```bash
docker compose down -v
```

---

# ▶️ Ejecución del backend

## Linux / macOS

```bash
./mvnw spring-boot:run
```

## Windows

```powershell
.\mvnw.cmd spring-boot:run
```

También puede utilizarse Maven instalado globalmente:

```bash
mvn spring-boot:run
```

Por defecto, Spring Boot inicia el servidor en:

```text
http://localhost:8080
```

Por ejemplo:

```http
GET http://localhost:8080/api/v1/reportes
```

---

# 🧪 Pruebas

El proyecto contiene pruebas unitarias y de integración.

Para ejecutar todos los tests:

### Linux / macOS

```bash
./mvnw test
```

### Windows

```powershell
.\mvnw.cmd test
```

Para ejecutar todo el proceso de verificación:

```bash
./mvnw clean verify
```

o:

```powershell
.\mvnw.cmd clean verify
```

Las pruebas incluyen servicios como:

```text
AuthService
ReporteService
CategoriaService
ZonaService
ComentarioService
ConfirmacionService
```

También existen pruebas de integración para distintos controllers.

La cobertura se genera mediante **JaCoCo**.

Después de ejecutar:

```bash
mvn clean verify
```

el reporte puede encontrarse normalmente en:

```text
target/site/jacoco/index.html
```

---

# 📮 Postman

El proyecto incluye una colección y un environment de Postman:

```text
postman/
├── UrbanFix.postman_collection.json
└── UrbanFix.postman_environment.json
```

Para utilizarlos:

1. Abrir Postman.
2. Seleccionar **Import**.
3. Importar ambos archivos.
4. Seleccionar el environment de UrbanFix.
5. Ejecutar primero el registro o login.
6. Utilizar el token recibido para probar los endpoints protegidos.

Esto permite probar los principales flujos de la API sin tener que crear las solicitudes manualmente.

---

# 📧 Eventos y notificaciones

UrbanFix implementa una arquitectura basada en eventos para algunas operaciones.

Por ejemplo:

```text
Registro de usuario
       │
       ▼
UsuarioRegistradoEvent
       │
       ▼
EmailNotificationListener
       │
       ▼
Email de bienvenida
```

También existe un evento para los cambios de estado:

```text
Cambio de estado
       │
       ▼
EstadoCambiadoEvent
       │
       ▼
EstadoNotificationListener
       │
       ▼
Notificación al usuario
```

Los listeners utilizan ejecución asíncrona mediante:

```java
@Async
```

y eventos asociados al resultado exitoso de una transacción mediante:

```java
@TransactionalEventListener(
    phase = TransactionPhase.AFTER_COMMIT
)
```

Esto evita bloquear innecesariamente la solicitud HTTP mientras se realizan tareas secundarias como las notificaciones.

Para desarrollo y pruebas también existe soporte para guardar correos generados en:

```text
email-output/
```

---

# 🔄 CI/CD

El proyecto utiliza **GitHub Actions**.

El workflow de integración continua se encuentra en:

```text
.github/workflows/ci.yml
```

Se ejecuta sobre:

```text
develop
main
```

tanto para `push` como para `pull_request`.

El pipeline realiza:

```text
Checkout
   │
   ▼
Configurar JDK 21
   │
   ▼
Levantar PostgreSQL 16
   │
   ▼
mvn clean verify
   │
   ▼
Construir JAR
   │
   ▼
Publicar artifact
```

El artefacto generado se almacena temporalmente en GitHub Actions.

También existe:

```text
.github/workflows/deploy.yml
```

que prepara un paquete de deployment cuando se realizan cambios en `main`.

Actualmente el paso de despliegue al servidor se encuentra preparado como **placeholder**, por lo que todavía debe configurarse el proveedor o servidor de producción definitivo.

---

# 📂 Estructura del proyecto

```text
src/
├── main/
│   ├── java/org/example/urbanfixbackend/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── entity/
│   │   │   └── enums/
│   │   ├── event/
│   │   │   └── listener/
│   │   ├── exception/
│   │   │   └── handler/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── security/
│   │   │   └── jwt/
│   │   ├── service/
│   │   │   └── impl/
│   │   ├── validation/
│   │   └── UrbanfixBackendApplication.java
│   │
│   └── resources/
│       ├── application.properties
│       └── templates/
│           └── email/
│
└── test/
    └── java/org/example/urbanfixbackend/
        ├── controller/
        ├── service/
        └── support/
```

Cada capa tiene una responsabilidad específica:

- **controller:** recepción de solicitudes HTTP.
- **service:** lógica de negocio.
- **repository:** acceso a PostgreSQL.
- **entity:** modelo persistente.
- **dto:** objetos de entrada y salida de la API.
- **mapper:** conversión entre entidades y DTOs.
- **security:** autenticación y autorización.
- **event:** eventos del dominio.
- **exception:** manejo de errores.
- **validation:** validaciones personalizadas.

---

# 🌿 Flujo de trabajo con Git

El repositorio utiliza principalmente:

```text
main
  ↑
develop
  ↑
feature/*
```

La rama `develop` funciona como rama principal de integración durante el desarrollo.

Para crear una nueva funcionalidad:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nombre-funcionalidad
```

Después de realizar los cambios:

```bash
git add .
git commit -m "feat: descripción del cambio"
git push origin feature/nombre-funcionalidad
```

Posteriormente se crea un **Pull Request hacia `develop`**.

Cuando una versión se encuentra lista para producción, `develop` puede integrarse hacia `main`.

---

# 🚀 Estado actual

El backend de UrbanFix cuenta actualmente con una base funcional que incluye:

- ✅ API REST versionada.
- ✅ PostgreSQL.
- ✅ Docker Compose.
- ✅ Arquitectura por capas.
- ✅ DTOs y mappers.
- ✅ JWT.
- ✅ BCrypt.
- ✅ Roles y autorización.
- ✅ CRUD de reportes.
- ✅ Categorías.
- ✅ Zonas.
- ✅ Comentarios.
- ✅ Confirmaciones.
- ✅ Historial de estados.
- ✅ Recuperación de contraseña.
- ✅ Eventos asíncronos.
- ✅ Templates de correo.
- ✅ Pruebas unitarias.
- ✅ Pruebas de integración.
- ✅ JaCoCo.
- ✅ Colección de Postman.
- ✅ GitHub Actions para integración continua.
- 🟡 Deployment de producción pendiente de configurar.

---

# 👨‍💻 UrbanFix

UrbanFix busca proporcionar una base tecnológica para mejorar la comunicación entre ciudadanos y entidades responsables de la infraestructura urbana, ofreciendo seguimiento, transparencia y participación comunitaria en la resolución de incidencias.

Backend:

```text
https://github.com/Urban-fix/Urbanfix-backend
```
