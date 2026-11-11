# Veterinaria San Marcos – Backend (Spring Boot)

Backend de microservicios con API Gateway, una base de datos por servicio, autenticación JWT con roles y Swagger.

| Módulo | Puerto | Responsabilidad | Base de datos |
|---|---:|---|---|
| gateway | 8080 | Entrada única, rutas, CORS, Swagger agregado | — |
| usuarios | 8081 | Registro, login, JWT, refresh, CRUD de usuarios | sanmarcos_usuarios |
| catalogo | 8082 | CRUD de productos, precios y stock | sanmarcos_catalogo |
| pedidos | 8083 | Compras, historial y estados | sanmarcos_pedidos |
| citas | 8084 | Citas veterinarias y mensajes de contacto | sanmarcos_citas |
| common | — | Biblioteca compartida (errores, seguridad, OpenAPI) | — |

Cada servicio: `controller`, `service`, `repository`, `model`, `dto`, `config`, `exception`.

## Requisitos

JDK 17 o superior (probado con JDK 21). `JAVA_HOME` debe apuntar a ese JDK. Maven no se instala: se usa el wrapper.

## Compilar y probar

```powershell
.\mvnw.cmd verify        # compila y ejecuta las 37 pruebas de integración
```

## Ejecutar (perfil h2, sin instalar base de datos)

Cada servicio en una terminal, en este orden:

```powershell
.\mvnw.cmd -pl usuarios spring-boot:run
.\mvnw.cmd -pl catalogo spring-boot:run
.\mvnw.cmd -pl pedidos  spring-boot:run
.\mvnw.cmd -pl citas    spring-boot:run
.\mvnw.cmd -pl gateway  spring-boot:run
```

(Antes, una vez: `.\mvnw.cmd -DskipTests install` para instalar `common`.)

Swagger de todos los servicios: <http://localhost:8080/swagger-ui.html>

## Ejecutar con MySQL

1. Crear las bases: `mysql -u root -p < sql/01_crear_bases_mysql.sql`
2. Definir `SPRING_PROFILES_ACTIVE=mysql` y `DB_PASSWORD` (y si hace falta `DB_URL`, `DB_USER`) al iniciar cada servicio.

Con Docker, el `compose.yaml` del proyecto completo levanta MySQL y todos los servicios.

## Cuentas iniciales

| Correo | Contraseña | Rol |
|---|---|---|
| admin@sanmarcos.cl | Admin1234 | ADMIN |
| vendedor@sanmarcos.cl | Vende1234 | VENDEDOR |
| cliente@gmail.com | Cliente123 | CLIENTE |

Se crean solo la primera vez; las contraseñas se pueden cambiar con las variables
`ADMIN_PASSWORD`, `VENDEDOR_PASSWORD` y `CLIENTE_PASSWORD`.

## Variables de entorno principales

| Variable | Uso | Valor por defecto |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `h2` o `mysql` | `h2` |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión MySQL de cada servicio | ver `application-mysql.yml` |
| `AUTH_JWKS_URL` | Dónde se obtiene la clave pública JWT | `http://localhost:8081/api/v1/auth/jwks` |
| `INTERNAL_API_KEY` | Clave entre pedidos y catálogo | valor local de ejemplo |
| `FRONTEND_ORIGINS` | Orígenes permitidos por CORS (gateway) | `http://localhost:5173,http://127.0.0.1:5173` |
| `KEYS_DIRECTORY` | Carpeta de las claves RSA (usuarios) | `./data/keys` |

No se suben a Git: `target/`, `data/` (bases H2 y claves RSA) ni `.env`.
