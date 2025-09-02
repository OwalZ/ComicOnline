# ComicOnline

Plataforma sencilla para gestionar y leer cómics/manga: catálogo, favoritos,
administración básica y endpoints REST. Construido con Spring Boot + SQLite +
Thymeleaf.

> Desarrollado por **Owal**.

## Características principales

- Catálogo con vista de lista y detalle.
- Registro / login con Spring Security (BCrypt).
- CRUD de cómics para administradores (`/admin/comics`).
- Favoritos para usuarios autenticados.
- API REST básica en `/api/comics` para listar y obtener detalles (JSON).
- Base de datos embebida SQLite (archivo `database.db` se crea automáticamente).
- Seeds iniciales: usuario `admin` (password `admin`) + 2 cómics de ejemplo.

## Stack

- Java 17
- Spring Boot 3 (Web, Data JPA, Security, Validation, Thymeleaf)
- SQLite (driver `org.xerial` + dialecto community)
- Maven
- Thymeleaf + extras de Spring Security

## Estructura rápida

```
src/main/java/com/example/comiconline
	├── config/ (seguridad, inicialización datos)
	├── controller/ (MVC + REST)
	├── model/ (entidades JPA: Comic, User, Role, etc.)
	├── repository/ (interfaces Spring Data)
	└── service/ (lógica de negocio)
src/main/resources
	├── templates/ (vistas Thymeleaf)
	├── static/ (css, assets)
	└── application.properties
```

## Rutas destacadas

| Ruta               | Método       | Descripción       | Público     |
| ------------------ | ------------ | ----------------- | ----------- |
| `/`                | GET          | Home / listado    | Sí          |
| `/login`           | GET/POST     | Autenticación     | Sí          |
| `/register`        | GET/POST     | Registro usuarios | Sí          |
| `/admin/comics`    | GET/POST/... | CRUD cómics       | Solo ADMIN  |
| `/favorites`       | GET          | Ver favoritos     | Autenticado |
| `/api/comics`      | GET          | Listado JSON      | Sí          |
| `/api/comics/{id}` | GET          | Detalle JSON      | Sí          |

## Seguridad

- Form Login personalizado (`/login`).
- BCrypt para contraseñas.
- Roles (ej: `ROLE_ADMIN`).
- Restricción de rutas vía `SecurityFilterChain`.

## Configuración

Archivo `application.properties` básico:

```
spring.datasource.url=jdbc:sqlite:database.db
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

Cambiar puerto: editar `server.port`. El archivo `database.db` se crea en la
raíz del proyecto.

## Requisitos previos

- Java 17+ (JDK).
- Maven (o usar wrapper si se añade en el futuro).

## Ejecución rápida

Windows:

```
run.bat
```

Linux / macOS (dar permisos la primera vez):

```
chmod +x run.sh
./run.sh
```

Esto lanzará `spring-boot:run` y descargará dependencias la primera vez.

### Modos de los scripts

```
run.bat jar       # Construir JAR y ejecutar
run.bat offline   # Pre-descargar dependencias y luego arrancar
./run.sh jar      # Igual en Unix
./run.sh offline  # Igual en Unix
```

Una vez empaquetado:

```
mvn -DskipTests clean package
java -jar target/comiconline-0.0.1-SNAPSHOT.jar
```

Aplicación: http://localhost:8080

## Desarrollo

Recompilar y recargar:

```
mvn spring-boot:run
```

Regenerar JAR sin tests:

```
mvn -DskipTests clean package
```

## Datos iniciales

Usuario administrador:

```
user: admin
pass: admin
```

Se recomienda cambiar la contraseña en entornos públicos.

## Próximas mejoras sugeridas

- Validaciones adicionales en formularios.
- Subida y gestión de imágenes de portada.
- Endpoints REST más completos para administración.
- Cache / paginación para catálogos grandes.
- Pruebas unitarias y de integración.

## Contribuciones

Issues y PRs son bienvenidos. Por favor describe claramente el cambio propuesto.

## Autor

Proyecto desarrollado por **Owal**.

## Licencia

Distribuido bajo la licencia MIT. Consulta el archivo `LICENSE` para más
detalles.
