# Guía para entender y presentar el proyecto

Sirve para estudiar antes de la presentación (60 % de la nota, individual) y la ronda de preguntas.
Cada punto dice **qué decir** y **qué archivo abrir** para demostrarlo.

## 0. Arquitectura en una frase

React (5173) habla solo con el **Gateway** (8080). El Gateway reenvía a cuatro microservicios Spring Boot
(**usuarios** 8081, **catalogo** 8082, **pedidos** 8083, **citas** 8084). Cada uno tiene **su propia base de datos**
y las mismas capas: `controller → service → repository → model`, más `dto`, `config` y `exception`.
`common` es una biblioteca compartida, no un servicio.

## 1. IE3.1.2 – Backend, base de datos, lógica de negocio y modelos

Recorrido sugerido con el catálogo (`backend/catalogo/src/main/java/cl/sanmarcos/catalogo`):

| Capa | Archivo | Qué mostrar |
|---|---|---|
| model | `model/Producto.java` | `@Entity`, `@Table`, `@Id`, `@Column(unique = true)` |
| repository | `repository/ProductosRepository.java` | Interfaz que extiende `JpaRepository`; Spring genera el SQL |
| service | `service/CatalogoService.java` | Regla de código duplicado (409) y `vender()` todo-o-nada con `@Transactional` |
| dto | `dto/ProductoRequest.java` | Validaciones `@NotBlank`, `@Min` |
| controller | `controller/ProductosController.java` | `@Valid @RequestBody`, delega en el servicio |
| exception | `common/.../ApiErrors.java` | Convierte excepciones en JSON `{status, mensaje, campos}` |

- **Conexión a BD:** `application.yml` (perfil por defecto `h2`), `application-h2.yml` y `application-mysql.yml`.
  Cambiar de H2 a MySQL no toca el código, solo el perfil (`SPRING_PROFILES_ACTIVE=mysql`).
- **Una base por servicio:** no hay `@ManyToOne` entre servicios; `Pedido.usuarioId` y `PedidoLinea.productoId` son solo números.
- **Reglas de negocio para nombrar:** RUN con dígito verificador (`usuarios/service/ReglasUsuario.java`), correo/RUN únicos,
  stock todo-o-nada, precio definido por el backend, veterinario sin tope de horario (`citas/service/CitasService.java`),
  transiciones de estado del pedido (`pedidos/service/PedidoStore.java`).

## 2. IE3.2.3 – API REST, CRUD y Swagger

- Rutas versionadas: `/api/v1/...`.
- CRUD completo de productos: `GET` lista y detalle, `POST` 201, `PUT` 200, `DELETE` 204.
- Códigos de estado: 400 validación, 401 sin token, 403 sin permiso, 404 no existe, 409 conflicto.
- Swagger: `http://localhost:8080/swagger-ui.html` (selector arriba a la derecha para cambiar de servicio).
  Se configura en `common/config/OpenApiConfig.java` y se documenta con `@Tag` y `@Operation` en los controladores.
- **Demostración en vivo:** en Swagger, `POST /auth/login` con el admin → copiar `accessToken` → botón **Authorize** →
  `POST /productos` (201) → repetirlo (409) → enviar precio negativo (400).

## 3. IE3.2.4 – Integración frontend ↔ backend

- `frontend/src/services/api.js`: única función `api()` con `fetch`, URL del Gateway, cabecera `Authorization`
  y `credentials: 'include'`.
- `backend/gateway/.../RoutesConfig.java`: tabla de rutas y política **CORS**.
- **Flujo para explicar (crear un producto):** formulario React → `api('/productos', POST)` → preflight `OPTIONS` →
  Gateway → `SecurityConfig` de catálogo valida JWT y rol → `ProductosController` → `CatalogoService` →
  `ProductosRepository` → tabla `productos` → respuesta 201 → `recargarProductos()` actualiza la pantalla.
- **Demostración:** abrir DevTools → pestaña Network → crear un producto y mostrar el `OPTIONS` y el `POST 201`.
- **Compra entre dos servicios** (`pedidos/service/PedidoWorkflow.java` y `client/CatalogoClient.java`):
  pedido PENDIENTE → llamada interna a `/internal/ventas/{id}` de catálogo → CONFIRMADO.
  La `Idempotency-Key` evita duplicar la compra si se reintenta.

## 4. IE3.3.4 – Autenticación con JWT y roles

- `usuarios/service/AuthService.java`: login con BCrypt, **access token** JWT (15 min, RS256) y **refresh token**
  (7 días) que se rota en cada uso; en la base solo se guarda su hash.
- `usuarios/security/JwtKeys.java`: par de claves RSA. La privada firma (solo en usuarios); la pública se publica en `/auth/jwks`.
- `common/security/SecuritySupport.java`: los otros servicios validan firma, emisor, audiencia y vencimiento,
  y convierten el claim `roles` en `ROLE_ADMIN`, etc.
- `*/config/SecurityConfig.java` de cada servicio: `hasRole("ADMIN")`, `hasAnyRole("ADMIN","VENDEDOR")`, `permitAll()`.
- El registro público siempre crea `CLIENTE` (`RegistroRequest` no tiene campo rol).

## 5. IE3.3.5 – Gestión de sesión en el frontend

- `frontend/src/context/AuthContext.jsx`: el usuario y su rol vienen del backend.
- El JWT vive **solo en memoria**; el refresh va en cookie **HttpOnly** (JavaScript no puede leerla → protege de XSS)
  con `SameSite=Strict` (protege de CSRF).
- Al recargar, `AuthContext` llama a `/auth/refresh` y recupera la sesión. `cargando` evita redirigir antes de tiempo.
- Si el token vence, `api.js` renueva y reintenta solo una vez; si falla, cierra la sesión.
- **Demostración:** iniciar sesión → F5 (sigue la sesión) → DevTools › Application › Local Storage (no hay token) →
  Cookies de `localhost:8080` (`sm_refresh` marcada HttpOnly) → cerrar sesión → F5 (ya no hay sesión).

## 6. IE3.3.6 – Restricciones de acceso en el frontend

- `frontend/src/components/RutaProtegida.jsx` y su uso en `App.jsx` (`roles={['ADMIN']}`…).
- `components/AdminLayout.jsx`: menú según rol (`{esAdmin && ...}`).
- `pages/admin/AdminProductos.jsx` y `AdminPedidos.jsx`: botones de escritura solo para ADMIN.
- **Demostración:** entrar como vendedor (solo Productos y Pedidos, sin botones) → escribir `/admin/usuarios` en la barra
  (redirige) → entrar como cliente y escribir `/admin` (redirige a la tienda).
- **Frase clave:** ocultar en el frontend es experiencia de usuario; la seguridad real es el 403 del backend.

## Preguntas frecuentes del docente

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué 5173 y 8080 son orígenes distintos? | Un origen es protocolo + host + puerto; cambia el puerto. Por eso se necesita CORS. |
| Si el OPTIONS funciona, ¿por qué el POST puede dar 403? | CORS solo autoriza al navegador a enviar; el permiso lo decide el microservicio según el rol del JWT. |
| ¿En qué capa va la regla de no duplicar códigos? | En el servicio (`CatalogoService.guardar`), y además `unique` en la entidad como respaldo. |
| ¿Qué conserva una línea de pedido si cambia el catálogo? | Nombre y precio de compra. |
| ¿Qué pasa si catálogo descuenta y pedidos pierde la respuesta? | El pedido queda PENDIENTE; al reintentar con la misma clave, catálogo devuelve el comprobante guardado sin descontar otra vez. |
| ¿Por qué no guardar el JWT en localStorage? | Cualquier script inyectado podría leerlo. En memoria + cookie HttpOnly no. |
| ¿Por qué BCrypt? | Es un hash lento y con sal: aunque se filtre la base no se obtienen las contraseñas. |
| ¿Por qué RS256 y no una clave compartida? | Los otros servicios solo tienen la clave pública: pueden verificar tokens pero no crearlos. |
| ¿Qué hace el Gateway? | Enruta y aplica CORS. No valida permisos ni tiene base de datos. |
| ¿Cómo se prueba? | 37 pruebas de integración (MockMvc + H2) y 43 pruebas de frontend (Vitest). |

## Pendientes del equipo antes de entregar

1. Completar los nombres de los integrantes en los dos documentos Word de `docs/`.
2. Terminar el documento **ERS** (no se generó aquí).
3. Revisar el diagrama de base de datos del Anexo 1 contra la sección 3 del documento de integración.
4. Subir `frontend/` y `backend/` a dos repositorios públicos de GitHub y agregar los enlaces a la entrega.
5. Probar `docker compose up --build` en un equipo con Docker Desktop.
